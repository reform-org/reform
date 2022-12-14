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

import loci.registry.*
import rescala.interface.*
import kofre.base.*
import loci.transmitter.*
import kofre.base.Lattice.*
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.*

/** @param name
  *   The name/type of the thing to sync
  * @param delta
  *   The payload to sync
  */
case class DeltaFor[A](name: String, delta: A)

/** @param api
  *   the rescala api to use
  * @param registry
  *   the connection registry to use
  * @param binding
  *   the binding for handling the update
  * @param dcl
  *   to split up the thing to sync into its containing lattices
  * @param bottom
  *   the neutral element of the thing to sync
  */
// TODO: First two arguments are always the same object in memory
class ReplicationGroup[Api <: RescalaInterface, A](
    val api: Api,
    registry: Registry,
    binding: Binding[DeltaFor[A] => Unit, DeltaFor[A] => Future[Unit]],
)(using
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
) {
  import api.*

  /** Map from concrete thing to handle to the event handler for that.
    */
  private var localListeners: Map[String, Evt[A]] = Map.empty

  /** Map from the concrete thing to handle to a map of remote ids and the thing to sync.
    */
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

    // observe changes to send them to every remote
    var observers = Map[RemoteRef, Disconnectable]()
    // store data that needs to be resent to a remote
    var resendBuffer = Map[RemoteRef, A]()

    // if there is unhandled data for this concrete thing then fire delta events for that as soon as it's registered
    unhandled.get(name) match {
      case None =>
      case Some(changes) =>
        changes.foreach((k, v) => deltaEvt.fire(v))
    }

    def registerRemote(remoteRef: RemoteRef): Unit = {
      // Lookup method to send data to remote
      val remoteUpdate: DeltaFor[A] => Future[Unit] = registry.lookup(binding, remoteRef)

      def sendUpdate(delta: A): Unit = {
        // the contents of the resend buffer and the delta need to be sent
        val allToSend = resendBuffer.get(remoteRef).merge(Some(delta)).get
        // remove from resend buffer for now
        resendBuffer = resendBuffer.removed(remoteRef)

        // functions that adds the data to the resend buffer
        def scheduleForLater(): Unit = {
          // add to resend buffer
          resendBuffer = resendBuffer.updatedWith(remoteRef) { current =>
            current.merge(Some(allToSend))
          }
          // note, it might be prudent to actually schedule some task that tries again,
          // but for now we just remember the value and piggyback on sending whenever the next update happens,
          // which might be never ...
        }

        if (remoteRef.connected) {
          // if the remote is connected try to send the data
          remoteUpdate(DeltaFor(name, allToSend)).onComplete {
            case Success(_) => // success
            case Failure(_) => scheduleForLater() // failure, add data to resend buffer
          }
        } else {
          // if the remote is not connected add the data to the resend buffer
          scheduleForLater()
        }
      }

      // Send full state to initialize remote
      val currentState = signal.readValueOnce
      // only send full state if it's not empty for efficiency
      if (currentState != bottom.empty) sendUpdate(currentState)

      // Whenever the crdt is changed propagate the delta
      val observer = signal.observe { s =>
        // note: isn't the resendbuffer also added in sendUpdate again?
        // combine the resend buffer and the current delta
        val deltaStateList = List(s) ++ resendBuffer.get(remoteRef).toList

        // reduce the state change to a single state for efficiency
        val combinedState = deltaStateList.reduceOption(DecomposeLattice[A].merge)

        combinedState.foreach(sendUpdate)
      }
      // add the handler for this remote to the observers
      observers += (remoteRef -> observer)
    }

    // if a remote joins register it to handle updates to it
    registry.remoteJoined.monitor(registerRemote)
    // also register all existing remotes
    registry.remotes.foreach(registerRemote)
    // remove remotes that disconnect
    registry.remoteLeft.monitor { remoteRef =>
      println(s"removing remote $remoteRef")
      observers(remoteRef).disconnect()
    }
    ()
  }
}
