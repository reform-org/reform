/*
Copyright 2022 https://github.com/phisn/ratable, The reform-org/reform contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package webapp.services

import colibri.*
import colibri.router.*
import colibri.router.Router
import loci.registry.Registry
import org.scalajs.dom.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import webapp.*
import webapp.pages.*
import kofre.decompose.containers.DeltaBufferRDT
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import java.util.concurrent.ThreadLocalRandom
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.decompose.interfaces.MVRegisterInterface.MVRegisterSyntax
import kofre.datatypes.TimedVal

import kofre.datatypes.TimedVal
import kofre.decompose.containers.DeltaBufferRDT
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegisterSyntax
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import kofre.decompose.interfaces.MVRegisterInterface.MVRegisterSyntax
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import loci.registry.Binding
import org.scalajs.dom.UIEvent
import org.scalajs.dom.html.{Input, LI}
import rescala.default.*
import rescala.extra.Tags.*
import scala.Function.const
import scala.collection.mutable
import scala.scalajs.js.timers.setTimeout
import scala.concurrent.Future
import kofre.base.DecomposeLattice
import kofre.base.Bottom
import loci.transmitter.RemoteRef
import scala.util.Success
import scala.util.Failure
import scribe.Execution.global
import webapp.Codecs.*

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import loci.registry.Registry
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import loci.serializer.jsoniterScala.given
import kofre.datatypes.PosNegCounter

class WebRTCService {
  val registry = new Registry

  val counter = createCounterRef()

  def createCounterRef(): (rescala.default.Signal[DeltaBufferRDT[PosNegCounter]], rescala.default.Evt[Int]) = {
    // a last writer wins register. This means the last value written is the actual value.
    val lastWriterWins = DeltaBufferRDT(replicaID, PosNegCounter.zero)

    // event that fires when the user wants to change the value
    val testChangeEvent = rescala.default.Evt[Int]();

    // event that fires when changes from other peers are received
    val deltaEvent = Evt[DottedName[PosNegCounter]]()

    // TODO FIXME Storing.storedAs persists this value

    // TaskData.scala
    // look at foldAll documentation+example
    val counterSignal: Signal[DeltaBufferRDT[PosNegCounter]] = Events.foldAll(lastWriterWins)(current => {
      Seq(
        // if the user changes the value, update the register with the new value
        testChangeEvent.act2({ v =>
          {
            current.resetDeltaBuffer().add(v)
          }
        }),
        // if we receive a delta from a peer, apply it
        deltaEvent.act2({ delta => current.resetDeltaBuffer().applyDelta(delta) }),
      )
    })

    distributeDeltaCRDT(counterSignal, deltaEvent, registry)(
      Binding[Dotted[PosNegCounter] => Unit]("counter"),
    )

    // magic to convert our counterSignal to the value inside
    val taskData = counterSignal.map(x => x.value)

    val t = new java.util.Timer()
    val task = new java.util.TimerTask {
      def run() = {
        val test: Int = taskData.now;
        println(test)
      }
    }
    t.schedule(task, 1000L, 1000L)

    (counterSignal, testChangeEvent)
  }

  def distributeDeltaCRDT[A](
      signal: Signal[DeltaBufferRDT[A]],
      deltaEvt: Evt[DottedName[A]],
      registry: Registry,
  )(binding: Binding[Dotted[A] => Unit, Dotted[A] => Future[Unit]])(implicit
      dcl: DecomposeLattice[Dotted[A]],
      bottom: Bottom[Dotted[A]],
  ): Unit = {
    // listens for deltas from peers
    registry.bindSbj(binding) { (remoteRef: RemoteRef, deltaState: Dotted[A]) =>
      // if we receive a delta from a peer, fire the deltaEvent
      deltaEvt.fire(DottedName(remoteRef.toString, deltaState))
    }

    var observers = Map[RemoteRef, Disconnectable]()
    var resendBuffer = Map[RemoteRef, Dotted[A]]()

    def registerRemote(remoteRef: RemoteRef): Unit = {
      val remoteUpdate: Dotted[A] => Future[Unit] = registry.lookup(binding, remoteRef)

      // Send full state to initialize remote
      val currentState = signal.readValueOnce.state
      if (currentState != bottom.empty) remoteUpdate(currentState)

      // Whenever the crdt is changed propagate the delta
      // Praktisch wäre etwas wie crdt.observeDelta
      val observer = signal.observe { s =>
        val deltaStateList = s.deltaBuffer.collect {
          case DottedName(replicaID, deltaState) if replicaID != remoteRef.toString => deltaState
        } ++ resendBuffer.get(remoteRef).toList

        val combinedState = deltaStateList.reduceOption(DecomposeLattice[Dotted[A]].merge)

        combinedState.foreach { s =>
          // in case the update fails this is the resend buffer
          val mergedResendBuffer = resendBuffer.updatedWith(remoteRef) {
            case None       => Some(s)
            case Some(prev) => Some(DecomposeLattice[Dotted[A]].merge(prev, s))
          }

          if (remoteRef.connected) {
            // send the update to the remote
            remoteUpdate(s).onComplete {
              case Success(_) =>
                // if the update was successful, we don't need to resend it
                resendBuffer = resendBuffer.removed(remoteRef)
              case Failure(_) =>
                // if the update failed, we need to resend it
                resendBuffer = mergedResendBuffer
            }
          } else {
            // if the remote is not connected we need to resend the delta later
            resendBuffer = mergedResendBuffer
          }
        }
      }
      observers += (remoteRef -> observer)
    }

    // if a remote joins, register it to send updates to it
    registry.remoteJoined.monitor(registerRemote)
    // also register all existing peers
    registry.remotes.foreach(registerRemote)
    // remove disconnected peers from observers
    registry.remoteLeft.monitor { remoteRef =>
      println(s"removing remote $remoteRef")
      observers(remoteRef).disconnect()
    }
    ()
  }
}
