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
package de.tu_darmstadt.informatik.st.reform.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonReader
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriter
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.entity.Entity
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.npm.IIndexedDB
import de.tu_darmstadt.informatik.st.reform.webrtc.ReplicationGroup
import kofre.base.*
import loci.registry.Registry
import rescala.default.*

import java.util.UUID
import scala.collection.mutable
import scala.concurrent.Future

type RepoAndValues[A] = (Repository[A], mutable.Map[String, A])

case class Repository[A](name: String, defaultValue: A)(using
    registry: Registry,
    indexedDb: IIndexedDB,
    dcl: Lattice[A],
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

  val ids: Signal[Set[String]] = Signal.fromFuture(idSynced).map(synced => synced.signal.map(_.set)).flatten

  implicit val valuesStorage: Storage[A] = Storage(name)

  private val valueSyncer = ReplicationGroup[A](name)

  val all: Signal[Seq[Synced[A]]] = {
    ids
      .map(ids => {
        val futures = ids.toSeq.map(load)
        val future = Future.sequence(futures)
        Signal.fromFuture(future)
      })
      .flatten
  }

  def find(id: String): Signal[Option[Synced[A]]] = Signal.dynamic {
    all.value.find(c => c.id == id)
  }

  private def load(id: String): Future[Synced[A]] = valueSyncer.getOrCreateAndSync(id)

  def create(initialValue: A): Future[Synced[A]] = {
    indexedDb.requestPersistentStorage()
    val id = UUID.randomUUID().toString
    valueSyncer
      .createAndSync(id, initialValue)
      .flatMap(value => {
        idSynced.flatMap(_.update(_.getOrElse(GrowOnlySet.empty).add(id)).map(_ => value))
      })
  }

  def getOrCreate(id: String): Future[Synced[A]] = {
    indexedDb.requestPersistentStorage()
    valueSyncer
      .getOrCreateAndSync(id)
      .flatMap(value => {
        idSynced.flatMap(_.update(_.getOrElse(GrowOnlySet.empty).add(id)).map(_ => value))
      })
  }
}

object Repository {

  implicit class EntityRepositoryOps[A <: Entity[A]](self: Repository[A]) {

    val existing: Signal[Seq[Synced[A]]] = Signal.dynamic { self.all.value.filter(_.signal.map(_.exists).value) }
  }
}
