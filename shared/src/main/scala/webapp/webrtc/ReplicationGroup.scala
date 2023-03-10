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
package webapp.webrtc

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.base.Lattice.*
import kofre.base.*
import loci.registry.Binding
import loci.registry.Registry
import loci.serializer.jsoniterScala.given
import loci.transmitter.*
import rescala.core.Disconnectable
import rescala.default.*

import webapp.given_ExecutionContext
import scala.concurrent.Future
import scala.util.*
import webapp.repo.Synced
import scala.annotation.nowarn
import webapp.repo.Storage
import webapp.npm.IIndexedDB
import webapp.Globals

/** @param name
  *   The name/type of the thing to sync
  * @param delta
  *   The payload to sync
  */
case class DeltaFor[A](name: String, delta: A)

/** @param name
  *   The name/type of the thing to sync
  * @param dcl
  *   to split up the thing to sync into its containing lattices
  * @param bottom
  *   the neutral element of the thing to sync
  */
class ReplicationGroup[A](name: String)(using
    registry: Registry,
    dcl: Lattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
    storage: Storage[A],
    indexeddb: IIndexedDB,
) {

  @volatile
  private var cache: Map[String, Future[Synced[A]]] = Map.empty

  def createAndSync(id: String, initialValue: A): Future[Synced[A]] = {
    synchronized {
      if (cache.contains(id)) {
        throw new Exception("This is not a new entity!")
      } else {
        val synced = storage
          .getOrDefault(id, initialValue)
          .map(value => {
            var synced = Synced(storage, id, Var(value))
            distributeDeltaRDT(id, synced)
            synced
          })
        cache = cache + (id -> synced)
        synced
      }
    }
  }

  def getOrCreateAndSync(id: String): Future[Synced[A]] = {
    synchronized {
      if (cache.contains(id)) {
        cache(id)
      } else {
        val synced = storage
          .getOrDefault(id, bottom.empty)
          .map(value => {
            var synced = Synced(storage, id, Var(value))
            distributeDeltaRDT(id, synced)
            synced
          })
        cache = cache + (id -> synced)
        synced
      }
    }
  }

  given deltaCodec: JsonValueCodec[DeltaFor[A]] = JsonCodecMaker.make

  given IdenticallyTransmittable[DeltaFor[A]] = IdenticallyTransmittable()
  given IdenticallyTransmittable[A] = IdenticallyTransmittable()

  given magicCodec: JsonValueCodec[Tuple2[Option[A], Option[String]]] = JsonCodecMaker.make

  private val binding = Binding[DeltaFor[A] => Future[A]](s"${Globals.VITE_PROTOCOL_VERSION}-${name}")

  registry.bindSbj(binding)((remoteRef: RemoteRef, payload: DeltaFor[A]) => {
    if (payload.name != "ids") {
      indexeddb.requestPersistentStorage
    }
    getOrCreateAndSync(payload.name).flatMap(_.update(v => v.getOrElse(bottom.empty).merge(payload.delta)))
  })

  def distributeDeltaRDT(
      name: String,
      synced: Synced[A],
  ): Unit = {
    // observe changes to send them to every remote
    var observers = Map[RemoteRef, Disconnectable]()
    // store data that needs to be resent to a remote
    var resendBuffer = Map[RemoteRef, A]()

    def registerRemote(remoteRef: RemoteRef): Unit = {
      // Lookup method to send data to remote
      val remoteUpdate = registry.lookup(binding, remoteRef)

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
      val currentState = synced.signal.readValueOnce
      // only send full state if it's not empty for efficiency
      if (currentState != bottom.empty) sendUpdate(currentState)

      // Whenever the crdt is changed propagate the delta
      val observer = synced.signal.observe { s =>
        // note: isn't the resendbuffer also added in sendUpdate again?
        // combine the resend buffer and the current delta
        val deltaStateList = List(s) ++ resendBuffer.get(remoteRef).toList

        // reduce the state change to a single state for efficiency
        val combinedState = deltaStateList.reduceOption(Lattice[A].merge)

        combinedState.foreach(sendUpdate)
      }
      // add the handler for this remote to the observers
      observers += (remoteRef -> observer)
    }

    // if a remote joins register it to handle updates to it
    registry.remoteJoined.foreach(registerRemote): @nowarn("msg=discarded expression")
    // also register all existing remotes
    registry.remotes.foreach(registerRemote)
    // remove remotes that disconnect
    registry.remoteLeft.monitor { remoteRef =>
      observers(remoteRef).disconnect()
    }: @nowarn("msg=discarded expression")
    ()
  }
}
