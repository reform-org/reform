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

import kofre.base.*
import org.scalajs.dom
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.navigationHeader
import webapp.repo.Repository
import webapp.repo.Synced
import webapp.services.DiscoveryService
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.{*, given}

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global

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
              cls := "border-b  dark:border-gray-700", // "hover:bg-violet-100 dark:hover:bg-violet-900 border-b hover:bg-gray-100 dark:hover:bg-gray-600 ",
              data.id := existingValue.map(v => v.id),
              uiAttributes.map(ui => {
                ui.renderEdit(s"form-${existingValue.map(_.id)}", editingValue)
              }),
              td(
                cls := "py-1 space-x-1 w-1/6",
                form(
                  idAttr := s"form-${existingValue.map(_.id)}",
                  onSubmit.foreach(e => {
                    e.preventDefault()
                    createOrUpdate()
                  }),
                ), {
                  existingValue match {
                    case Some(p) => {
                      List(
                        button(
                          cls := "btn btn-active p-2 h-fit min-h-10 border-0",
                          formId := s"form-${existingValue.map(_.id)}",
                          `type` := "submit",
                          idAttr := "add-entity-button",
                          "Save edit",
                        ),
                        button(
                          cls := "btn btn-active p-2 h-fit min-h-10 border-0",
                          idAttr := "add-entity-button",
                          "Cancel",
                          onClick.foreach(_ => cancelEdit()),
                        ),
                      )
                    }
                    case None => {
                      button(
                        cls := "btn btn-active p-2 h-fit min-h-10 border-0",
                        formId := s"form-${existingValue.map(_.id)}",
                        `type` := "submit",
                        idAttr := "add-entity-button",
                        "Add Entity",
                      )
                    }
                  }
                },
                existingValue.map(p => {
                  button(
                    cls := "tooltip btn btn-error btn-square p-2 h-fit min-h-10 border-0",
                    data.tip := "Delete",
                    "X",
                    onClick.foreach(_ => removeEntity(p)),
                  )
                }),
              ),
            ),
          )
          Var(res)
        }
        case None => {
          val res: Signal[Option[VNode]] = existingValue match {
            case Some(syncedEntity) => {
              val res = syncedEntity.value.map(p => {
                val res = if (p.exists.get.getOrElse(true)) {
                  Some(
                    tr(
                      cls := "border-b hover:bg-violet-100 dark:hover:bg-gray-600 dark:border-gray-700", // "border-b hover:bg-gray-100 dark:hover:bg-violet-900 dark:border-violet-900",
                      data.id := syncedEntity.id,
                      uiAttributes.map(ui => {
                        ui.render(p)
                      }),
                      td(
                        cls := "py-1 space-x-1 w-1/6",
                        button(
                          cls := "btn btn-active p-2 h-fit min-h-10 border-0",
                          "Edit",
                          onClick.foreach(_ => startEditing()),
                        ),
                        button(
                          cls := "tooltip btn btn-error btn-square p-2 h-fit min-h-10 border-0",
                          data.tip := "Delete",
                          "X",
                          onClick.foreach(_ => removeEntity(syncedEntity)),
                        ),
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
    val yes = window.confirm(s"Do you really want to delete the entity \"${p.value.now.identifier.get}\"?")
    if (yes) {
      p.update(p => p.get.withExists(false))
        .onComplete(value => {
          if (value.isFailure) {
            // TODO FIXME show Toast
            value.failed.get.printStackTrace()
            window.alert(value.failed.get.getMessage().nn)
          }
        })
    }
  }

  def cancelEdit(): Unit = {
    editingValue.set(None)
  }

  def createOrUpdate(): Unit = {
    val editingNow = editingValue.now
    (existingValue match {
      case Some(existing) => {
        existing
          .update(p => {
            p.get.merge(editingNow.get)
          })
          .onComplete(value => {
            if (value.isFailure) {
              // TODO FIXME show Toast
              value.failed.get.printStackTrace()
              window.alert(value.failed.get.getMessage().nn)
            }
          })
        editingValue.set(None)
      }
      case None => {
        repository
          .create()
          .flatMap(entity => {
            editingValue.set(Some(bottom.empty.default))
            //  TODO FIXME we probably should special case initialization and not use the event
            entity.update(p => {
              p.getOrElse(bottom.empty).merge(editingNow.get)
            })
          })
          .onComplete(value => {
            if (value.isFailure) {
              // TODO FIXME show Toast
              value.failed.get.printStackTrace()
              window.alert(value.failed.get.getMessage().nn)
            }
          })
      }
    })
  }

  private def startEditing(): Unit = {
    editingValue.set(Some(existingValue.get.value.now))
  }
}

abstract class EntityPage[T <: Entity[T]](repository: Repository[T], uiAttributes: Seq[UIAttribute[T, ? <: Any]])(using
    bottom: Bottom[T],
    lattice: Lattice[T],
) extends Page {

  private val newUserRow: EntityRow[T] =
    EntityRow[T](repository, None, Var(Some(bottom.empty.default)), uiAttributes)

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
  ): VNode = {
    navigationHeader(
      div(
        cls := "relative overflow-x-auto shadow-md sm:rounded-lg pt-4 ", // " px-4 py-4 items-center w-full  mx-auto my-5 bg-white rounded-lg shadow-md",
        table(
          cls := "w-full text-left table-auto border-collapse ", // border-separate border-spacing-y-4
          // cls := "table-auto",
          thead(
            tr(
              uiAttributes.map(a => th(cls := "px-6 py-0 border-b-2 dark:border-gray-500", a.label)),
              th(cls := "px-6 py-0 border-b-2 dark:border-gray-500", "Stuff"),
            ),
          ),
          tbody(
            renderEntities(repository.all),
            newUserRow.render(),
          ),
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
