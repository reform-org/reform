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

import kofre.datatypes.PosNegCounter
import scala.concurrent.Future
import scala.scalajs.js.JSON
import loci.registry.Binding
import rescala.default.{Events, Evt, Signal}
import com.github.plokhotnyuk.jsoniter_scala.core.{readFromString, writeToString}
import webapp.Codecs.{myReplicaID, given}
import loci.serializer.jsoniterScala.given
import concurrent.ExecutionContext.Implicits.global
import webapp.npm.IdbKeyval
import webapp.Project
import webapp.GrowOnlySet
import java.util.UUID
import kofre.syntax.PermIdMutate

case class EventedProjects(
    signal: rescala.default.Signal[GrowOnlySet[String]],
    addNewProjectEvent: rescala.default.Evt[String],
)

object ProjectsService {
  val projects = createProjectsRef()

  def createProjectsRef(): Future[EventedProjects] = {
    // restore from indexeddb
    val init: Future[GrowOnlySet[String]] = IdbKeyval
      .get[scala.scalajs.js.Object]("projects")
      .toFuture
      .map(value =>
        value.toOption
          .map(value => readFromString[GrowOnlySet[String]](JSON.stringify(value)))
          .getOrElse(GrowOnlySet(Set.empty)),
      );

    init.map(init => {
      val projects = init

      // event that fires when the user wants to change the value
      val changeEvent = rescala.default.Evt[String]();

      // event that fires when changes from other peers are received
      val deltaEvent = Evt[GrowOnlySet[String]]()

      // look at foldAll documentation+example
      val projectsSignal: Signal[GrowOnlySet[String]] = Events.foldAll(projects)(current => {
        Seq(
          // if the user wants to change the value, update the register accordingly
          changeEvent.act2({ v =>
            {
              GrowOnlySet(current.set + v)
            }
          }),
          // if we receive a delta from a peer, apply it
          deltaEvent.act2({ delta => current.merge(delta) }),
        )
      })

      projectsSignal.observe(
        value => {
          // write the updated value to persistent storage
          // TODO FIXME this is async which means this is not robust
          IdbKeyval.set("projects", JSON.parse(writeToString(value)))
        },
        fireImmediately = true,
      )

      WebRTCService.distributeDeltaCRDT(projectsSignal, deltaEvent, WebRTCService.registry)(
        Binding[GrowOnlySet[String] => Unit]("projects"),
      )

      EventedProjects(projectsSignal, changeEvent)
    })
  }
}
