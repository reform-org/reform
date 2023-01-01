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


import org.scalajs.dom
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.*
import webapp.given
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

private class NewProjectRow {

  private val name = Var("")
  private val maxHours = Var("")
  private val account = Var("")

  val onNewProject: Evt[EventedProject] = Evt[EventedProject]()

  def render(): VNode =
    tr(
      td(
        input(
          value <-- name,
          onInput.value --> name,
          placeholder := "New Project Name",
        ),
      ),
      td(
        input(
          `type` := "number",
          value <-- maxHours,
          onInput.value --> maxHours,
          placeholder := "0",
        ),
      ),
      td(
        input(
          value <-- account,
          onInput.value --> account,
          placeholder := "Some account",
        ),
      ),
      td(
        button(
          cls := "btn",
          "Add Project",
          onClick.foreach(_ => addNewProject()),
        ),
      ),
    )

  private def addNewProject(): Unit = {
    try {
      val _name = validateName();
      val _max_hours = validateMaxHours();
      val _account = validateAccount();
      val project = ProjectService.createOrGetProject(UUID.randomUUID().toString());
      project.map(project => {
        // we probably should special case initialization and not use the event
        project.changeEvent.fire(p => {
          p.withName(_name).withAddedMaxHours(_max_hours).withAccountName(_account)
        })
        onNewProject.fire(project)

        name.set("")
        maxHours.set("")
        account.set("")
      })
    } catch {
      case e: Exception => window.alert(e.getMessage)
    }
  }

  private def validateMaxHours(): Int = {
    val maxHours = this.maxHours.now
    val hours = maxHours.toIntOption

    if (hours.isEmpty || hours.get < 0) {
      throw new Exception("Invalid max hours: " + maxHours)
    }

    hours.get
  }

  private def validateName(): String = {
    val name = this.name.now

    if (name.isBlank) {
      throw new Exception("Invalid empty name")
    }

    name.strip
  }

  private def validateAccount(): Option[String] = {
    val account = this.account.now
    if (account.isBlank) None else Some(account)
  }
}

case class ProjectsPage() extends Page {

  private val newProjectRow: NewProjectRow = NewProjectRow()

  // add the project uuid to the projects list
  newProjectRow.onNewProject.observe(p => ProjectsService.projects.map(_.addNewProjectEvent.fire(p.id)))

  def render(using services: Services): VNode = {
    div(
      navigationHeader,
      table(
        cls := "table-auto",
        thead(
          tr(
            th("Project"),
            th("Max Hours"),
            th("Account"),
            th("Stuff"),
          ),
        ),
        tbody(
          ProjectsService.projects.map(
            _.signal.map(projects =>
              renderProjects(projects.set.toList.map(projectId => ProjectService.createOrGetProject(projectId))),
            ),
          ),
          newProjectRow.render(),
        ),
      ),
    )
  }

  private def renderProjects(projects: List[Future[EventedProject]]): List[VNode] =
    projects.map(p =>
      tr(
        td(p.map(_.signal.map(_.name))),
        td(p.map(_.signal.map(_.maxHours))),
        td(p.map(_.signal.map(_.accountName))),
        button(
          cls := "btn",
          "Delete",
          onClick.foreach(_ => p.map(removeProject)),
        ),
      ),
    )

  private def removeProject(p: EventedProject): Unit = {
    val yes = window.confirm(s"Do you really want to delete the project \"${p.signal.now.name}\"?")
    if (yes) {
      // ProjectsService.projects.transform(_.filterNot(_ == p))
    }
  }
}
