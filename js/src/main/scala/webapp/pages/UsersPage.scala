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
import webapp.services.Page
import webapp.Repositories.users

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.UUID
import kofre.time.VectorClock

private class UserRow(existingValue: Option[Synced[User]], editingValue: Var[Option[User]]) {

  def updateUsername(x: String) = {
    editingValue.transform(value => {
      value.map(p => p.withUsername(x))
    })
  }

  def updateRole(x: String) = {
    editingValue.transform(value => {
      value.map(p => p.withRole(x))
    })
  }

  def updateComment(x: String) = {
    editingValue.transform(value => {
      value.map(p => p.withComment(Some(x)))
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
                  value := editingNow.username.mkString("/"),
                  onInput.value --> {
                    val evt = Evt[String]()
                    evt.observe(x => updateUsername(x))
                    evt
                  },
                  placeholder := "Username",
                ),
              ),
              td(
                input(
                  value := editingNow.role.mkString("/"),
                  onInput.value --> {
                    val evt = Evt[String]()
                    evt.observe(x => updateRole(x))
                    evt
                  },
                  placeholder := "admin",
                ),
              ),
              td(
                input(
                  value := editingNow.comment.mkString("/"),
                  onInput.value --> {
                    val evt = Evt[String]()
                    evt.observe(x => updateComment(x))
                    evt
                  },
                  placeholder := "",
                ),
              ),
              td(
                {
                  existingValue match {
                    case Some(p) => {
                      List(
                        button(
                          cls := "btn",
                          idAttr := "add-User-button",
                          "Save edit",
                          onClick.foreach(_ => createOrUpdate()),
                        ),
                        button(
                          cls := "btn",
                          idAttr := "add-User-button",
                          "Cancel",
                          onClick.foreach(_ => cancelEdit()),
                        ),
                      )
                    }
                    case None => {
                      button(
                        cls := "btn",
                        idAttr := "add-User-button",
                        "Add User",
                        onClick.foreach(_ => createOrUpdate()),
                      )
                    }
                  }
                },
                existingValue.map(p => {
                  button(cls := "btn", "Delete", onClick.foreach(_ => removeUser(p)))
                }),
              ),
            ),
          )
          Var(res)
        }
        case None => {
          val res: Signal[Option[VNode]] = existingValue match {
            case Some(syncedUser) => {
              val res = syncedUser.signal.map(p => {
                val res = if (p.exists.headOption.getOrElse(true)) {
                  Some(
                    tr(
                      data.id := syncedUser.id,
                      td(duplicateValuesHandler(p.username.map(_.toString()))),
                      td(duplicateValuesHandler(p.role.map(_.toString()))),
                      td(
                        duplicateValuesHandler(p.comment.map(_.toString())),
                      ),
                      td(
                        button(
                          cls := "btn",
                          "Edit",
                          onClick.foreach(_ => edit()),
                        ),
                        button(cls := "btn", "Delete", onClick.foreach(_ => removeUser(syncedUser))),
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

  def removeUser(p: Synced[User]): Unit = {
    val yes = window.confirm(s"Do you really want to delete the User \"${p.signal.now.username}\"?")
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
        users
          .create()
          .map(user => {
            //  TODO FIXME we probably should special case initialization and not use the event
            user.update(p => {
              p.merge(editingNow.get)
            })
            editingValue.set(Some(User.empty.withExists(true).withUsername("").withComment(Some("")).withRole("")))
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

case class UsersPage() extends Page {

  private val newUserRow: UserRow =
    UserRow(None, Var(Some(User.empty.withExists(true).withUsername("").withComment(Some("")).withRole(""))))

  def render(using services: Services): VNode = {
    div(
      navigationHeader,
      table(
        cls := "table-auto",
        thead(
          tr(
            th("Username"),
            th("Role"),
            th("Comment"),
            th("Stuff"),
          ),
        ),
        tbody(
          renderUsers(users.all),
          newUserRow.render(),
        ),
      ),
    )
  }

  private def renderUsers(
      Users: Signal[List[Synced[User]]],
  ) =
    Users.map(
      _.map(syncedUser => {
        UserRow(Some(syncedUser), Var(None)).render()
      }),
    )
}
