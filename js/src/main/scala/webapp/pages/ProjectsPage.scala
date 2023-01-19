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
import webapp.given
import webapp.components.navigationHeader
import webapp.repo.Synced
import webapp.webrtc.WebRTCService
import webapp.Repositories.projects
import webapp.services.Page

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.UUID
import kofre.time.VectorClock

private class ProjectRow(existingValue: Option[Synced[Project]], editingValue: Var[Option[Project]]) {

  def updateName(x: String) = {
    editingValue.transform(value => {
      value.map(p => p.withName(x))
    })
  }

  def updateMaxHours(x: String) = {
    editingValue.transform(value => {
      value.map(p => p.withMaxHours(x.toInt))
    })
  }

  def updateAccountName(x: String) = {
    editingValue.transform(value => {
      value.map(p => p.withAccountName(Some(x)))
    })
  }

  def render() = {
    editingValue.map(editingNow => {
      val res = editingNow match {
        case Some(editingNow) => {
          val res = Some(
            tr(
              td(
                input(
                  value := editingNow.name.mkString("/"),
                  onInput.value --> {
                    val evt = Evt[String]()
                    evt.observe(x => updateName(x))
                    evt
                  },
                  placeholder := "New Project Name",
                ),
              ),
              td(
                input(
                  value := editingNow.maxHours.mkString("/"),
                  onInput.value --> {
                    val evt = Evt[String]()
                    evt.observe(x => updateMaxHours(x))
                    evt
                  },
                  placeholder := "0",
                ),
              ),
              td(
                input(
                  value := editingNow.accountName.map(_.getOrElse("")).mkString("/"),
                  onInput.value --> {
                    val evt = Evt[String]()
                    evt.observe(x => updateAccountName(x))
                    evt
                  },
                  placeholder := "Some account",
                ),
              ),
              td(
                {
                  existingValue match {
                    case Some(p) => {
                      List(
                        button(
                          cls := "btn",
                          idAttr := "add-project-button",
                          "Save edit",
                          onClick.foreach(_ => createOrUpdate()),
                        ),
                        button(
                          cls := "btn",
                          idAttr := "add-project-button",
                          "Cancel",
                          onClick.foreach(_ => cancelEdit()),
                        ),
                      )
                    }
                    case None => {
                      button(
                        cls := "btn",
                        idAttr := "add-project-button",
                        "Add Project",
                        onClick.foreach(_ => createOrUpdate()),
                      )
                    }
                  }
                },
                existingValue.map(p => {
                  button(cls := "btn", "Delete", onClick.foreach(_ => removeProject(p)))
                }),
              ),
            ),
          )
          Var(res)
        }
        case None => {
          val res: Signal[Option[VNode]] = existingValue match {
            case Some(syncedProject) => {
              val res = syncedProject.signal.map(p => {
                val res = if (p.exists.headOption.getOrElse(true)) {
                  Some(
                    tr(
                      data.id := syncedProject.id,
                      td(duplicateValuesHandler(p.name.map(_.toString()))),
                      td(duplicateValuesHandler(p.maxHours.map(_.toString()))),
                      td(
                        duplicateValuesHandler(p.accountName.map(_.getOrElse("no accout name"))),
                      ),
                      td(
                        button(
                          cls := "btn",
                          "Edit",
                          onClick.foreach(_ => edit()),
                        ),
                        button(cls := "btn", "Delete", onClick.foreach(_ => removeProject(syncedProject))),
                      ),
                    ),
                  )
                } else {
                  None
                }
                res
              })
              res
            }
            case None => Var(None)
          }
          res
        }
      }
      res
    })
  }

  def removeProject(p: Synced[Project]): Unit = {
    val yes = window.confirm(s"Do you really want to delete the project \"${p.signal.now.name}\"?")
    if (yes) {
      p.update(p => p.withExists(false))
    }
  }

  def cancelEdit(): Unit = {
    editingValue.set(None)
  }

  def createOrUpdate(): Unit = {
    val editingNow = editingValue.now
    (existingValue match {
      case Some(existing) => {
        existing.update(p => {
          p.merge(editingNow.get)
        })
        editingValue.set(None)
      }
      case None => {
        projects
          .create()
          .map(project => {
            //  TODO FIXME we probably should special case initialization and not use the event
            project.update(p => {
              p.merge(editingNow.get)
            })
            editingValue.set(Some(Project.empty.withExists(true).withAccountName(Some("")).withName("")))
          })
      }
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

  private val newProjectRow: ProjectRow =
    ProjectRow(None, Var(Some(Project.empty.withExists(true).withAccountName(Some("")).withName(""))))

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
  ) =
    projects.map(
      _.map(syncedProject => {
        ProjectRow(Some(syncedProject), Var(None)).render()
      }),
    )
}
