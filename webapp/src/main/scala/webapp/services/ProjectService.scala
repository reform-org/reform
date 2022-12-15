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

import kofre.decompose.containers.DeltaBufferRDT
import kofre.datatypes.PosNegCounter
import scala.concurrent.Future
import scala.scalajs.js.JSON
import kofre.syntax.DottedName
import loci.registry.Binding
import kofre.dotted.Dotted
import rescala.default.{Events, Evt, Signal}
import com.github.plokhotnyuk.jsoniter_scala.core.{readFromString, writeToString}
import webapp.Codecs.{myReplicaID, given}
import loci.serializer.jsoniterScala.given
import concurrent.ExecutionContext.Implicits.global
import webapp.npm.IdbKeyval
import webapp.Project
import scala.collection.mutable

case class EventedProject(
    id: String,
    signal: rescala.default.Signal[Project],
    changeEvent: rescala.default.Evt[Project => Project],
)

object ProjectService {

  private val projectMap: mutable.Map[String, Future[EventedProject]] = mutable.Map.empty

  def createOrGetProject(id: String): Future[EventedProject] = {
    projectMap.getOrElseUpdate(id, createProjectRef(id))
  }

  // TODO FIXME for project creation this could non non-async? Or should it write into the database at creation? Or does this simply create too complex code?
  private def createProjectRef(id: String): Future[EventedProject] = {
    // restore from indexeddb
    val init: Future[Project] = IdbKeyval
      .get[scala.scalajs.js.Object](s"project-$id")
      .toFuture
      .map(value =>
        value.toOption
          .map(value => readFromString[Project](JSON.stringify(value)))
          .getOrElse(Project.empty),
      );

    init.map(init => {
      val project = init

      // event that fires when the user wants to change the value
      val changeEvent = rescala.default.Evt[Project => Project]();

      // event that fires when changes from other peers are received
      val deltaEvent = Evt[Project]()

      // look at foldAll documentation+example
      val projectSignal: Signal[Project] = Events.foldAll(project)(current => {
        Seq(
          // if the user wants to increase the value, update the register accordingly
          changeEvent.act2(_(current)),
          // if we receive a delta from a peer, apply it
          deltaEvent.act2(delta => {
            println("apply delta")
            current.merge(delta)
          }),
        )
      })

      projectSignal.observe(
        value => {
          // write the updated value to persistent storage
          // TODO FIXME this is async which means this is not robust
          IdbKeyval.set(s"project-$id", JSON.parse(writeToString(value)))
        },
        fireImmediately = true,
      )

      WebRTCService.distributeDeltaCRDT(projectSignal, deltaEvent, WebRTCService.registry)(
        Binding[Project => Unit](s"project-$id"),
      )

      EventedProject(id, projectSignal, changeEvent)
    })
  }
}
