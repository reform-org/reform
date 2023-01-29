/*
Copyright 2022 The reform-org/reform contributors

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
package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import kofre.base.*
import rescala.default.*
import webapp.*

import java.util.UUID
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import loci.registry.Registry
import webapp.npm.IIndexedDB

case class Repository[A](name: String, defaultValue: A)(using
    registry: Registry,
    indexedDb: IIndexedDB,
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
) {

  private val idStorage = IdSetStorage(name)

  private val syncedIds = SyncedIdSet(name)
  syncedIds.syncWithStorage(idStorage)

  private val valuesStorage = Storage[A](name, defaultValue)

  private val valueSyncer = Syncer[A](name)

  private val cache: mutable.Map[String, Synced[A]] = mutable.Map.empty

  def create(): Future[Synced[A]] =
    getOrCreate(UUID.randomUUID.toString)

  val all: Signal[List[Synced[A]]] = {
    syncedIds.ids
      .map(ids => {
        val futures = ids.toList.map(getOrCreate)
        val future = Future.sequence(futures)
        Signals.fromFuture(future)
      })
      .flatten
  }

  private def getOrCreate(id: String): Future[Synced[A]] = {
    if (cache.contains(id)) {
      return Future(cache(id))
    }

    createSyncedFromRepo(id)
  }

  private def createSyncedFromRepo(id: String): Future[Synced[A]] =
    valuesStorage
      .getOrDefault(id)
      .map(project => {
        val synced = valueSyncer.sync(id, project)
        cache.put(id, synced)
        syncedIds.add(id)
        updateRepoVersionOnChangesReceived(synced)
        synced
      })

  private def updateRepoVersionOnChangesReceived(synced: Synced[A]): Unit =
    synced.signal.observe(value => valuesStorage.set(synced.id, value))

}
