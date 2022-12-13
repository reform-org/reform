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
package webapp.pages

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.*
import webapp.components.navigationHeader


import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import webapp.given
import webapp.components.navigationHeader
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.UUID

private class NewUserRow {

  private val username = Var("")
  private val role = Var("")
  private val comment = Var("")

  //val onNewProject: Evt[EventedProject] = Evt[EventedProject]()

  def render(): VNode =
    tr(
      td(
        input(
          value <-- username,
          onInput.value --> username,
          placeholder := "New User Name",
        ),
      ),
      td(
        input(
          value <-- role,
          onInput.value --> role,
          placeholder := "User role",
        ),
      ),
      td(
        input(
          value <-- comment,
          onInput.value --> comment,
          placeholder := "Some comment",
        ),
      ),
      td(
        button(
          cls := "btn",
          "Create User",
          //onClick.foreach(_ => addNewProject()),
        ),
      ),
    )

  
}




case class UsersPage() extends Page {

  //private val NewUserRow: NewUserRow = NewUserRow()

  //newProjectRow.onNewProject.observe(p => ProjectsService.projects.map(_.addNewProjectEvent.fire(p.id)))

  def render(using services: Services): VNode = {
    div(
      navigationHeader,
      div(
        cls := "p-1",
        h1(cls := "text-4xl text-center", "User page"),
      ),
      table(
        cls := "table-auto",
        thead(
          tr(
            th("User"),
            th("Role"),
            th("Comment"),
            th("Stuff"),
          ),
        ),
        tbody(
          //ProjectsService.projects.map(
          //  _.signal.map(projects =>
          //    renderProjects(projects.set.toList.map(projectId => ProjectService.createOrGetProject(projectId))),
          //  ),
          //),
          //newProjectRow.render(),
        ),
      ),
    )
  }

  
}
