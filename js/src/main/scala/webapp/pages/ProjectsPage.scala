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
import webapp.*
import webapp.given
import webapp.components.navigationHeader
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import outwatch.dsl.svg.idAttr
import webapp.given
import webapp.components.navigationHeader
import webapp.repo.Synced
import webapp.webrtc.WebRTCService
import webapp.Repositories.projects
import webapp.services.Page
import webapp.utils.memo

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.UUID

class CustomEditing(initialEditing: Boolean) {
  println("CustomEditing")

  var editing = Var(initialEditing)
}

private class ProjectRow(existingValue: Option[Synced[Project]], initialEditing: Boolean) {

  def render() = {
    var name = Var("")
    var maxHours = Var("")
    var account = Var("")
    var editing: CustomEditing = CustomEditing(initialEditing)

    def removeProject(p: Synced[Project]): Unit = {
      val yes = window.confirm(s"Do you really want to delete the project \"${p.signal.now.name}\"?")
      if (yes) {
        p.update(p => p.withExists(false))
      }
    }

    def addNewProject(): Unit = {
      try {
        val _name = validateName()
        val _max_hours = validateMaxHours()
        val _account = validateAccount()
        val _exists = true

        val project = projects.create()
        project.map(project => {
          // we probably should special case initialization and not use the event
          project.update(p => {
            // TODO IMPORTANT for editing we should only update the values that changed
            p.withName(_name).withMaxHours(_max_hours).withAccountName(_account).withExists(_exists)
          })

          name.set("")
          maxHours.set("")
          account.set("")
        })
      } catch {
        case e: Exception => window.alert(e.getMessage)
      }
    }

    def validateMaxHours(): Int = {
      val maxHoursNow = maxHours.now
      val hours = maxHoursNow.toIntOption

      if (hours.isEmpty || hours.get < 0) {
        throw new Exception("Invalid max hours: " + maxHoursNow)
      }

      hours.get
    }

    def validateName(): String = {
      val nameNow = name.now

      if (nameNow.isBlank) {
        throw new Exception("Invalid empty name")
      }

      nameNow.strip
    }

    def validateAccount(): Option[String] = {
      val accountNow = account.now
      if (accountNow.isBlank) None else Some(accountNow)
    }

    def extractedHandler() = {
      name.set(existingValue.get.signal.now.name)
      maxHours.set(existingValue.get.signal.now.maxHours.toString())
      account.set(existingValue.get.signal.now.accountName)
      editing.editing.set(true)
    }

    editing.editing.map(editingNow => {
      if (editingNow) {
        Some(
          tr(
            // attributes.key := p.id,
            // data.id := p.id,
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
            {
              existingValue match {
                case Some(p) => {
                  td(
                    button(
                      cls := "btn",
                      idAttr := "add-project-button",
                      "Add Project",
                      onClick.foreach(_ => addNewProject()),
                    ),
                  ),
                }
                case None => {
                  td(
                    button(
                      cls := "btn",
                      idAttr := "add-project-button",
                      "Save edit",
                      onClick.foreach(_ => saveEdit()),
                    ),
                  )
                }
              }
            },
            existingValue.map(p => {
              td(
                button(cls := "btn", "Delete", onClick.foreach(_ => removeProject(p))),
              )
            }),
          ),
        )
      } else {
        existingValue match {
          case Some(p) =>
            Some(
              tr(
                attributes.key := p.id,
                data.id := p.id,
                td(p.signal.map(_.name)),
                td(p.signal.map(_.maxHours)),
                td(p.signal.map(_.accountName)),
                td(
                  button(
                    cls := "btn",
                    "Edit",
                    // onClick("dfs") --> editing,
                    { println(editing.hashCode()); editing.editing }.set(false), // here it works
                    onClick.foreach(_ => extractedHandler()),
                  ),
                  button(cls := "btn", "Delete", onClick.foreach(_ => removeProject(p))),
                ),
              ),
            )
          case None => None
        }
      }
    })
  }
}

case class ProjectsPage() extends Page {

  private val newProjectRow: ProjectRow = ProjectRow(None, true)

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
          renderProjects(projects.all),
          newProjectRow.render(),
        ),
      ),
    )
  }

  private def renderProjects(
      projects: Signal[List[Synced[Project]]],
  ): rescala.default.Signal[List[rescala.default.Signal[outwatch.VModifier]]] =
    projects.map(
      _.map(syncedProject => {
        syncedProject.signal.map(p => {
          if (p.exists) {
            ProjectRow(Some(syncedProject), false).render()
          } else {
            None
          }
        })
      }),
    )
}
