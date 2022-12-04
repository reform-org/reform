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
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.*
import webapp.given
import webapp.components.navigationHeader

case class Project(name: String, maxHours: Int, account: Option[String])

private class NewProjectRow {

  private val name = Var("")
  private val maxHours = Var("")
  private val account = Var("")

  val onNewProject: Evt[Project] = Evt[Project]()

  def render(): VNode =
    tr(
      td(
        input(
          name,
          placeholder := "New Project Name",
          onInput.value --> name,
        ),
      ),
      td(
        input(
          `type` := "number",
          maxHours,
          placeholder := "0",
          onInput.value --> maxHours,
        ),
      ),
      td(
        input(
          account,
          placeholder := "Some account",
          onInput.value --> account,
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
      val p = Project(validateName(), validateMaxHours(), validateAccount())
      onNewProject.fire(p)
      name.set("")
      maxHours.set("")
      account.set("")
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

case class ProjectPage(id: String) extends Page {

  private val projects: Var[List[Project]] = Var(
    List(
      Project("FOP", Integer.MAX_VALUE, Some("KW")),
      Project("Reform", 69, None),
      Project("Off-Topic", 0, Some("HK")),
    ),
  )

  private val newProjectRow: NewProjectRow = NewProjectRow()

  newProjectRow.onNewProject.observe(p => projects.transform(_.appended(p)))

  def render(using services: Services): VNode =
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
          projects.map(renderProjects),
          newProjectRow.render(),
        ),
      ),
    )

  private def renderProjects(projects: List[Project]): List[VNode] =
    projects.map(p =>
      tr(
        td(p.name),
        td(p.maxHours),
        td(p.account.getOrElse("-")),
        button(
          cls := "btn",
          "Delete",
          onClick.foreach(_ => removeProject(p)),
        ),
      ),
    )

  private def removeProject(p: Project): Unit = {
    val yes = window.confirm(s"Do you really want to delete the project \"${p.name}\"?")
    if (yes) {
      projects.transform(_.filterNot(_ == p))
    }
  }
}
