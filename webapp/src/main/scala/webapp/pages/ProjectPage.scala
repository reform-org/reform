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

case class ProjectPage(id: String) extends Page {

  private val projects: Var[List[Project]] = Var(
    List(
      Project("FOP", Integer.MAX_VALUE, Some("KW")),
      Project("Reform", 69, None),
      Project("Off-Topic", 0, Some("HK")),
    ),
  )

  private val name = Var("")

  def render(using services: Services): VNode =
    div(
      navigationHeader,
      input(
        name,
        onInput.value --> name,
      ),
      button(
        cls := "btn",
        "New",
        onClick.foreach(_ => addProject(name.now)),
      ),
      table(
        cls := "table-auto",
        thead(
          tr(
            th("Project"),
            th("Max Hours"),
            th("Account"),
          ),
        ),
        projects.map(renderProjects),
      ),
    )

  private def renderProjects(projects: List[Project]): VNode =
    tbody(projects.map { p =>
      tr(
        td(p.name),
        td(p.maxHours),
        td(p.account.getOrElse("-")),
        button(
          cls := "btn",
          "Delete",
          onClick.foreach(_ => removeProject(p)),
        ),
      )
    })

  def removeProject(p: Project): Unit = {
    val yes = window.confirm(s"Do you really want to delete the project \"${p.name}\"?")
    if (yes) {
      projects.transform(_.filterNot(_ == p))
    }
  }

  def addProject(name: String): Unit = {
    projects.transform(_.appended(Project(name, 0, None)))
  }
}
