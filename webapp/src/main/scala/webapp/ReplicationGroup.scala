/*
Copyright 2022 https://github.com/rescala-lang/REScala

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
package webapp

import kofre.base.{Bottom, DecomposeLattice, Lattice}
import loci.registry.{Binding, Registry}
import loci.transmitter.RemoteRef
import rescala.interface.RescalaInterface
import scribe.Execution.global
import kofre.base.Lattice.Operators

import scala.concurrent.Future
import scala.util.{Failure, Success}

case class DeltaFor[A](name: String, delta: A)

class ReplicationGroup[Api <: RescalaInterface, A](
    val api: Api,
    registry: Registry,
    binding: Binding[DeltaFor[A] => Unit, DeltaFor[A] => Future[Unit]],
)(using
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
) {
  import api.*

  private var localListeners: Map[String, Evt[A]] = Map.empty
  private var unhandled: Map[String, Map[String, A]] = Map.empty

  registry.bindSbj(binding) { (remoteRef: RemoteRef, payload: DeltaFor[A]) =>
    localListeners.get(payload.name) match {
      case Some(handler) => handler.fire(payload.delta)
      case None =>
        unhandled = unhandled.updatedWith(payload.name) { current =>
          current.merge(Some(Map(remoteRef.toString -> payload.delta)))
        }
    }
  }

  def distributeDeltaRDT(
      name: String,
      signal: Signal[A],
      deltaEvt: Evt[A],
  ): Unit = {
    require(!localListeners.contains(name), s"already registered a RDT with name $name")
    localListeners = localListeners.updated(name, deltaEvt)

    var observers = Map[RemoteRef, Disconnectable]()
    var resendBuffer = Map[RemoteRef, A]()

    unhandled.get(name) match {
      case None =>
      case Some(changes) =>
        changes.foreach((k, v) => deltaEvt.fire(v))
    }

    def registerRemote(remoteRef: RemoteRef): Unit = {
      val remoteUpdate: DeltaFor[A] => Future[Unit] = registry.lookup(binding, remoteRef)

      def sendUpdate(delta: A): Unit = {
        val allToSend = (resendBuffer.get(remoteRef).merge(Some(delta))).get
        resendBuffer = resendBuffer.removed(remoteRef)

        def scheduleForLater() = {
          resendBuffer = resendBuffer.updatedWith(remoteRef) { current =>
            current.merge(Some(allToSend))
          }
          // note, it might be prudent to actually schedule some task that tries again,
          // but for now we just remember the value and piggyback on sending whenever the next update happens,
          // which might be never ...
        }

        if (remoteRef.connected) {
          remoteUpdate(DeltaFor(name, allToSend)).onComplete {
            case Success(_) =>
            case Failure(_) => scheduleForLater()
          }
        } else {
          scheduleForLater()
        }
      }

      // Send full state to initialize remote
      val currentState = signal.readValueOnce
      if (currentState != bottom.empty) sendUpdate(currentState)

      // Whenever the crdt is changed propagate the delta
      // Praktisch wäre etwas wie crdt.observeDelta
      val observer = signal.observe { s =>
        val deltaStateList = List(s) ++ resendBuffer.get(remoteRef).toList

        val combinedState = deltaStateList.reduceOption(DecomposeLattice[A].merge)

        combinedState.foreach(sendUpdate)
      }
      observers += (remoteRef -> observer)
    }

    registry.remoteJoined.monitor(registerRemote)
    registry.remotes.foreach(registerRemote)
    registry.remoteLeft.monitor { remoteRef =>
      println(s"removing remote $remoteRef")
      observers(remoteRef).disconnect()
    }
    ()
  }
}
