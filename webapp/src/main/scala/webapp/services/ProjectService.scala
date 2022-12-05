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
import webapp.Codecs.{replicaID, given}
import loci.serializer.jsoniterScala.given
import concurrent.ExecutionContext.Implicits.global
import webapp.npm.IdbKeyval
import webapp.Project

case class EventedProject(
    signal: rescala.default.Signal[DeltaBufferRDT[Project]],
    setNameEvent: rescala.default.Evt[String],
)

object ProjectService {
  val project = createProjectRef()

  def createProjectRef(): Future[EventedProject] = {
    // restore counter from indexeddb
    val init: Future[Project] = Future { Project.empty }
    /*IdbKeyval
        .get[scala.scalajs.js.Object]("counter")
        .toFuture
        .map(value =>
          value.toOption
            .map(value => readFromString[Project](JSON.stringify(value)))
            .getOrElse(Project.zero),
        );*/

    init.map(init => {
      // a positive negative counter. This means that concurrent updates will be merged by adding them together.
      val project = DeltaBufferRDT(replicaID, init)

      // event that fires when the user wants to change the value
      val changeEvent = rescala.default.Evt[String]();

      // event that fires when changes from other peers are received
      val deltaEvent = Evt[DottedName[Project]]()

      // look at foldAll documentation+example
      val projectSignal: Signal[DeltaBufferRDT[Project]] = Events.foldAll(project)(current => {
        Seq(
          // if the user wants to increase the value, update the register accordingly
          changeEvent.act2({ v =>
            {
              current.resetDeltaBuffer().set_name(v)
            }
          }),
          // if we receive a delta from a peer, apply it
          deltaEvent.act2({ delta => current.resetDeltaBuffer().applyDelta(delta) }),
        )
      })

      projectSignal.observe(
        value => {
          // write the updated value to persistent storage
          // TODO FIXME this is async which means this is not robust
          // IdbKeyval.set("counter", JSON.parse(writeToString(value.state.store)))
        },
        fireImmediately = true,
      )

      WebRTCService.distributeDeltaCRDT(projectSignal, deltaEvent, WebRTCService.registry)(
        Binding[Dotted[Project] => Unit]("project"),
      )

      EventedProject(projectSignal, changeEvent)
    })
  }
}
