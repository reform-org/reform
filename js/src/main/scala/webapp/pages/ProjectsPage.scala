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

private class ProjectRow(existingValue: Option[Synced[Project]], editingValue: Var[Option[Project]]) {

  def render() = {
    editingValue.map(editingNow => {
      editingNow match {
        case Some(editingNow) => {
        Some(
          tr(
            // attributes.key := p.id,
            // data.id := p.id,
            td(
              input(
                value := editingNow.name,
                onInput.value --> {
                  val evt = Evt[String]()
                  evt.observe( x => {
                    // this probably has the same bug
                    editingValue.transform(value => {
                      value.map(p => p.withName(x))
                    })
                  })
                  evt
                },
                placeholder := "New Project Name",
              ),
            ),
            td(
              input(
                `type` := "number",
                value := editingNow.maxHours.toString(),
                onInput.value --> {
                  val evt = Evt[String]()
                  evt.observe( x => {
                    // this probably has the same bug
                    editingValue.transform(value => {
                      value.map(p => p.withMaxHours(x.toInt))
                    })
                  })
                  evt
                },
                placeholder := "0",
              ),
            ),
            td(
              input(
                value := editingNow.accountName,
                onInput.value --> {
                  val evt = Evt[String]()
                  evt.observe( x => {
                    // this probably has the same bug
                    editingValue.transform(value => {
                      value.map(p => p.withAccountName(Some(x)))
                    })
                  })
                  evt
                },
                placeholder := "Some account",
              ),
            ), {
              existingValue match {
                case Some(p) => {
                  td(
                    button(
                      cls := "btn",
                      idAttr := "add-project-button",
                      "Add Project",
                      onClick.foreach(_ => createOrUpdate()),
                    ),
                  )
                }
                case None => {
                  td(
                    button(
                      cls := "btn",
                      idAttr := "add-project-button",
                      "Save edit",
                      onClick.foreach(_ => createOrUpdate()),
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
      }
      case None => {
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
                    onClick.foreach(_ => edit()),
                  ),
                  button(cls := "btn", "Delete", onClick.foreach(_ => removeProject(p))),
                ),
              ),
            )
          case None => None
        }
      }
    }})
  }

  def removeProject(p: Synced[Project]): Unit = {
    val yes = window.confirm(s"Do you really want to delete the project \"${p.signal.now.name}\"?")
    if (yes) {
      p.update(p => p.withExists(false))
    }
  }

  def createOrUpdate(): Unit = {
    (existingValue match {
      case Some(existing) => {
        Future(existing)
      }
      case None => {
        projects.create()
      }
    }).map(project => {
        // we probably should special case initialization and not use the event
        project.update(p => {
          p.withExists(true).merge(editingValue.now.get)
        })
      })
  }
/*
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
*/

  def edit() = {
    editingValue.set(Some(existingValue.get.signal.now))
  }
}

case class ProjectsPage() extends Page {

  private val newProjectRow: ProjectRow = ProjectRow(None, Var(Some(Project.empty)))

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
            ProjectRow(Some(syncedProject), Var(None)).render()
          } else {
            None
          }
        })
      }),
    )
}
