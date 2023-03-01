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
import com.github.plokhotnyuk.jsoniter_scala.core.JsonReader
import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriter
import scala.collection.mutable
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import webapp.webrtc.ReplicationGroup
import webapp.utils.Seqnal.*
import webapp.entity.Entity

type RepoAndValues[A] = (Repository[A], mutable.Map[String, A])

case class Repository[A](name: String, defaultValue: A)(using
    registry: Registry,
    indexedDb: IIndexedDB,
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
) {

  def bottomEmpty: A = bottom.empty

  def latticeMerge(left: A, right: A): A = dcl.merge(left)(right)

  given mapCodec: JsonValueCodec[mutable.Map[String, A]] = JsonCodecMaker.make

  def encodeRepository(out: JsonWriter): Unit = {
    var values: mutable.Map[String, A] = mutable.Map()
    all.now.foreach(f => values += (f.id -> f.signal.now))
    mapCodec.encodeValue(values, out)
  }

  def decodeRepository(in: JsonReader): RepoAndValues[A] = {
    var values = mapCodec.decodeValue(in, mutable.Map.empty)
    (this, values)
  }

  implicit val idStorage: Storage[GrowOnlySet[String]] = Storage(name)

  private val idSyncer = ReplicationGroup[GrowOnlySet[String]](name + "-ids")

  private val idSynced = idSyncer.getOrCreateAndSync("ids")

  val ids: Signal[Set[String]] = Signals.fromFuture(idSynced).map(synced => synced.signal.map(_.set)).flatten

  implicit val valuesStorage: Storage[A] = Storage(name)

  private val valueSyncer = ReplicationGroup[A](name)

  val all: Signal[Seq[Synced[A]]] = {
    ids
      .map(ids => {
        val futures = ids.toSeq.map(get)
        val future = Future.sequence(futures)
        Signals.fromFuture(future)
      })
      .flatten
  }

  // TODO FIXME make this private and create a public getOption
  private def get(id: String): Future[Synced[A]] = valueSyncer.sync(id)

  def create(initialValue: A): Future[Synced[A]] = {
    indexedDb.requestPersistentStorage
    val id = UUID.randomUUID().toString
    valueSyncer
      .createAndSync(id, initialValue)
      .flatMap(value => {
        idSynced.flatMap(_.update(_.getOrElse(GrowOnlySet.empty).add(id)).map(_ => value))
      })
  }

  def getOrCreate(id: String): Future[Synced[A]] = {
    indexedDb.requestPersistentStorage
    valueSyncer
      .getOrCreateAndSync(id)
      .flatMap(value => {
        idSynced.flatMap(_.update(_.getOrElse(GrowOnlySet.empty).add(id)).map(_ => value))
      })
  }
}

object Repository {

  implicit class EntityRepositoryOps[A <: Entity[A]](self: Repository[A]) {

    val existing: Signal[Seq[Synced[A]]] = self.all
      .flatMap(
        _.filterSignal(_.signal.map(_.exists)),
      )
  }
}
