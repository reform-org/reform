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

import webapp.*
import webapp.pages.*
import webapp.Codecs.*

import outwatch.*
import outwatch.dsl.*

import java.util.concurrent.ThreadLocalRandom

import kofre.decompose.interfaces.MVRegisterInterface.MVRegisterSyntax
import kofre.decompose.containers.DeltaBufferRDT
import kofre.datatypes.TimedVal
import kofre.datatypes.PosNegCounter
import kofre.base.{Bottom, DecomposeLattice}

import loci.registry.{Binding, Registry}
import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import loci.transmitter.RemoteRef
import loci.serializer.jsoniterScala.given

import org.scalajs.dom.html.{Input, LI}
import org.scalajs.dom.*
import org.scalajs.dom

import rescala.default.*
import rescala.extra.Tags.*

import scribe.Execution.global

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.Function.const
import scala.collection.mutable
import scala.scalajs.js.timers.setTimeout
import scala.util.{Failure, Success}
import scala.reflect.Selectable.*
import scala.scalajs.js
import loci.serializer.jsoniterScala.given
import kofre.datatypes.PosNegCounter
import scala.scalajs.js.JSON

object WebRTCService {
  val registry = new Registry

  // receives the changes from all peers, also responsible for sharing own data
  def distributeDeltaCRDT[A](
      signal: Signal[A],
      deltaEvt: Evt[A],
      registry: Registry,
  )(
      binding:
      // the first parameter of the binding is the receive side
      // the second parameter is what registry.lookup returns for you to update the remote
      Binding[A => Unit, A => Future[Unit]],
  )(implicit
      dcl: DecomposeLattice[A],
      bottom: Bottom[A],
  ): Unit = {
    // listens for deltas from peers
    registry.bindSbj(binding) { (remoteRef: RemoteRef, deltaState: A) =>
      // when we receive a delta from a peer, the deltaEvent will be fired
      deltaEvt.fire(deltaState)
    }

    // for every remote store an observer that detects changes and sends them to that remote
    var observers = Map[RemoteRef, Disconnectable]()

    // for every remote store the data that needs to be resent to that remote
    var resendBuffer = Map[RemoteRef, A]()

    // gets executed everytime a remote joins
    def registerRemote(remoteRef: RemoteRef): Unit = {
      val remoteUpdate: A => Future[Unit] = registry.lookup(binding, remoteRef)

      // Send full state to initialize remote
      val currentState = signal.readValueOnce
      if (currentState != bottom.empty) remoteUpdate(currentState)

      // Whenever the crdt is changed propagate the delta
      val observer = signal.observe { s =>
        // all changes that currently need to be sent to the remote
        val deltaStateList = List(s) ++ resendBuffer.get(remoteRef).toList

        // combine the list of changes into a single change for efficiency
        val combinedState = deltaStateList.reduceOption(DecomposeLattice[A].merge)

        // try sending the change to the remote
        combinedState.foreach { s =>
          // in case the update fails this is the resend buffer
          val mergedResendBuffer = resendBuffer.updatedWith(remoteRef) {
            case None =>
              // if the resend buffer is empty add the current changes to the resend buffer
              Some(s)
            case Some(prev) =>
              // if the resend buffer is not empty append/merge the current changes to the resend buffer
              Some(DecomposeLattice[A].merge(prev, s))
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
