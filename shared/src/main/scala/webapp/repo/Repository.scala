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
import loci.registry.Registry
import rescala.default.*
import webapp.*
import webapp.npm.IIndexedDB

import java.util.UUID
import webapp.given_ExecutionContext
import scala.concurrent.Future
import scala.annotation.nowarn

case class Repository[A](name: String, defaultValue: A)(using
    registry: Registry,
    indexedDb: IIndexedDB,
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
) {

  private val idStorage: Storage[GrowOnlySet[String]] = Storage(name, GrowOnlySet.empty)

  private val idSyncer = Syncer[GrowOnlySet[String]](name + "-ids")

  private val idSynced = idSyncer.sync(idStorage, "ids", GrowOnlySet.empty)

  val ids: Signal[Set[String]] =
    idSynced.signal.map(_.set)

  idStorage
    .getOrDefault("ids")
    .map(ids => {
      idSynced.update(_.getOrElse(GrowOnlySet.empty).union(ids))
    }): @nowarn("msg=discarded expression")

  private val valuesStorage = Storage[A](name, defaultValue)

  private val valueSyncer = Syncer[A](name)

  @volatile
  private var cache: Map[String, Future[Synced[A]]] = Map.empty

  def create(): Future[Synced[A]] =
    getOrCreate(UUID.randomUUID.toString)

  val all: Signal[List[Synced[A]]] = {
    ids
      .map(ids => {
        val futures = ids.toList.map(getOrCreate)
        val future = Future.sequence(futures)
        Signals.fromFuture(future)
      })
      .flatten
  }

  def getOrCreate(id: String): Future[Synced[A]] = {
    this.synchronized {
      if (cache.contains(id)) {
        cache(id)
      } else {
        val synced = createSyncedFromRepo(id)
        cache += (id -> synced)
        synced
      }
    }
  }

  private def createSyncedFromRepo(id: String): Future[Synced[A]] =
    valuesStorage
      .getOrDefault(id)
      .map(project => valueSyncer.sync(valuesStorage, id, project))
      .flatMap(value => {
        idSynced.update(_.getOrElse(GrowOnlySet.empty).add(id)).map(_ => value)
      })

  // if we update a value:
  // we should set the value using a future so we can return an error
  // to set the value we should start a transaction
  // that transaction should read the current value in storage
  // then it should merge it with our new value
  // and then it should write it to storage
  // then we should probably somehow notify the other tabs
  // that the value has been updated. these update notification
  // could in theory come the same way that loci updates are received.
}
