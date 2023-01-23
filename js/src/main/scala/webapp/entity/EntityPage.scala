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
package webapp.entity

import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import kofre.base.*
import kofre.time.VectorClock
import org.scalajs.dom
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.Repositories.users
import webapp.components.navigationHeader
import webapp.repo.{Repository, Synced}
import webapp.services.Page
import webapp.webrtc.WebRTCService
import webapp.{*, given}

import java.util.UUID
import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private class EntityRow[T <: Entity[T]](
    repository: Repository[T],
    existingValue: Option[Synced[T]],
    editingValue: Var[Option[T]],
    uiAttributes: Seq[UIAttribute[T, ? <: Any]],
)(using bottom: Bottom[T], lattice: Lattice[T]) {

  def render() = {
    editingValue.map(editingNow => {
      val res = editingNow match {
        case Some(_) => {
          val res = Some(
            tr(
              uiAttributes.map(ui => {
                ui.renderEdit(editingValue)
              }),
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
                val res = if (p.exists.get.getOrElse(true)) {
                  Some(
                    tr(
                      data.id := syncedEntity.id,
                      uiAttributes.map(attr => {
                        td(duplicateValuesHandler(attr.getter(p).getAll.map(_.toString())))
                      }),
                      td(
                        button(
                          cls := "btn",
                          "Edit",
                          onClick.foreach(_ => startEditing()),
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
    val yes = window.confirm(s"Do you really want to delete the entity \"${p.signal.now.identifier.get}\"?")
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

  private def startEditing(): Unit = {
    editingValue.set(Some(existingValue.get.signal.now))
  }
}

abstract class EntityPage[T <: Entity[T]](repository: Repository[T], uiAttributes: Seq[UIAttribute[T, ? <: Any]])(using
    bottom: Bottom[T],
    lattice: Lattice[T],
) extends Page {

  private val newUserRow: EntityRow[T] =
    EntityRow[T](repository, None, Var(Some(bottom.empty)), uiAttributes)

  def render(using services: Services): VNode = {
    div(
      navigationHeader,
      table(
        cls := "table-auto",
        thead(
          tr(
            uiAttributes.map(a => th(a.placeholder)),
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
        EntityRow[T](repository, Some(syncedEntity), Var(None), uiAttributes).render()
      }),
    )
}
