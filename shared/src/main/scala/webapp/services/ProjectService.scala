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

object ProjectService {

  private val idsRepo = IdSetRepository("project")

  private val syncedIds = SyncedIdSet("project")
  syncedIds.syncWithRepo(idsRepo)

  private val valuesRepo = Repository[Project]("project", Project.empty)

  private val valueSyncer = Syncer[Project]("project")

  private val cache: mutable.Map[String, Synced[Project]] = mutable.Map.empty

  def getOrCreateSyncedProject(id: String): Future[Synced[Project]] = {
    if (cache.contains(id)) {
      return Future(cache(id))
    }

    createSyncedFromRepo(id)
  }

  private def createSyncedFromRepo(id: String): Future[Synced[Project]] =
    valuesRepo
      .getOrDefault(id)
      .map(project => {
        val synced = valueSyncer.sync(id, project)
        cache.put(id, synced)
        syncedIds.add(id)
        updateRepoVersionOnChangesReceived(synced)
        synced
      })

  private def updateRepoVersionOnChangesReceived(synced: Synced[Project]): Unit =
    synced.signal.observe(value => valuesRepo.set(synced.id, value))

  val all: Signal[List[Synced[Project]]] = {
    syncedIds.ids
      .map(ids => {
        val futures = ids.toList.map(getOrCreateSyncedProject)
        val future = Future.sequence(futures)
        Signals.fromFuture(future)
      })
      .flatten
  }
}
