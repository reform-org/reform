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
import webapp.repo.Repository
import kofre.base.Bottom

private class EntityRow[T](repository: Repository[T], existingValue: Option[Synced[T]], editingValue: Var[Option[T]])(using bottom: Bottom[T]) {

  def render() = {
    editingValue.map(editingNow => {
      val res = editingNow match {
        case Some(editingNow) => {
          val res = Some(
            tr(
              editingNow._username.render[User](
                (u, x) => {
                  u.withUsername(x)
                },
                va => va.toString(),
                editingValue,
                editingNow,
              ),
              editingNow._role.render[User](
                (u, x) => {
                  u.withRole(x)
                },
                va => va.toString(),
                editingValue,
                editingNow,
              ),
              editingNow._comment.render[User](
                (u, x) => {
                  u.withComment(Some(x))
                },
                va => va.getOrElse("no comment").toString(),
                editingValue,
                editingNow,
              ),
              td(
                {
                  existingValue match {
                    case Some(p) => {
                      List(
                        button(
                          cls := "btn",
                          idAttr := "add-entity-button",
                          "Save edit",
                          onClick.foreach(_ => createOrUpdate()),
                        ),
                        button(
                          cls := "btn",
                          idAttr := "add-entity-button",
                          "Cancel",
                          onClick.foreach(_ => cancelEdit()),
                        ),
                      )
                    }
                    case None => {
                      button(
                        cls := "btn",
                        idAttr := "add-entity-button",
                        "Add Entity",
                        onClick.foreach(_ => createOrUpdate()),
                      )
                    }
                  }
                },
                existingValue.map(p => {
                  button(cls := "btn", "Delete", onClick.foreach(_ => removeEntity(p)))
                }),
              ),
            ),
          )
          Var(res)
        }
        case None => {
          val res: Signal[Option[VNode]] = existingValue match {
            case Some(syncedEntity) => {
              val res = syncedEntity.signal.map(p => {
                val res = if (p._exists.get().getOrElse(true)) {
                  Some(
                    tr(
                      data.id := syncedEntity.id,
                      td(duplicateValuesHandler(p._username.getAll().map(_.toString()))),
                      td(duplicateValuesHandler(p._role.getAll().map(_.toString()))),
                      td(
                        duplicateValuesHandler(p._comment.getAll().map(_.toString())),
                      ),
                      td(
                        button(
                          cls := "btn",
                          "Edit",
                          onClick.foreach(_ => edit()),
                        ),
                        button(cls := "btn", "Delete", onClick.foreach(_ => removeEntity(syncedEntity))),
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

  def removeEntity(p: Synced[T]): Unit = {
    val yes = window.confirm(s"Do you really want to delete the entity \"${p.signal.now._username.get()}\"?")
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
        repository
          .create()
          .map(entity => {
            //  TODO FIXME we probably should special case initialization and not use the event
            entity.update(p => {
              p.merge(editingNow.get)
            })
            editingValue.set(Some(bottom.empty))
          })
      }
    })
  }

  def edit() = {
    editingValue.set(Some(existingValue.get.signal.now))
  }
}

case class EntityPage[T](repository: Repository[T])(using bottom: Bottom[T]) extends Page {

  private val newUserRow: EntityRow[T] =
    EntityRow[T](None, Var(Some(bottom.empty)))

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
          renderEntities(repository.all),
          newUserRow.render(),
        ),
      ),
    )
  }

  private def renderEntities(
      entities: Signal[List[Synced[T]]],
  ) =
    entities.map(
      _.map(syncedEntity => {
        EntityRow[T](Some(syncedEntity), Var(None)).render()
      }),
    )
}
