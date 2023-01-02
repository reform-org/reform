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
package webapp.services

import webapp.Project
import webapp.repo.*

import scala.collection.mutable
import scala.concurrent.Future
import rescala.default.*

import concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import webapp.GrowOnlySet
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import webapp.DeltaFor
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

object ProjectService {

  implicit val codecDeltaForGrowOnlySetString: JsonValueCodec[DeltaFor[GrowOnlySet[String]]] = JsonCodecMaker.make

  private val idsSyncer = Syncer[GrowOnlySet[String]]("project-ids", GrowOnlySet.empty)
  private val valueSyncer = Syncer[Project]("project", Project.empty)

  private val syncedIds: Synced[GrowOnlySet[String]] = idsSyncer.getOrDefault("ids")

  def getOrCreateSyncedProject(id: String): Synced[Project] = {
    addId(id)
    valueSyncer.getOrDefault(id)
  }

  val ids: Signal[Set[String]] =
    syncedIds.signal.map(_.set)

  val all: Signal[List[Synced[Project]]] = {
    ids.map(ids => {
      ids.toList.map(getOrCreateSyncedProject)
    })
  }

  def addId(id: String): Unit =
    syncedIds.update(_.add(id))
}
