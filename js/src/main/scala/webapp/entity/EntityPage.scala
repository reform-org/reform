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
import outwatch.*
import outwatch.dsl.*
import rescala.default
import rescala.default.*
import webapp.components.navigationHeader
import webapp.repo.Repository
import webapp.repo.Synced
import webapp.services.DiscoveryService
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.{*, given}
import webapp.components.{Modal, ModalButton}
import webapp.services.{ToastMode, Toaster}
import webapp.utils.Futures.*
import webapp.utils.Seqnal.*
import webapp.components.common.*

import webapp.components.Icons
import webapp.given_ExecutionContext

sealed trait EntityValue[T]
case class Existing[T](value: Synced[T]) extends EntityValue[T]
// TODO FIXME: This should not be an Option but otherwise we need to find a generic way to set an Option and non-option Var
case class New[T](value: Var[Option[T]]) extends EntityValue[T]

private class EntityRow[T <: Entity[T]](
    val repository: Repository[T],
    val value: EntityValue[T],
    val uiAttributes: Seq[UIAttribute[T, ? <: Any]],
)(using bottom: Bottom[T], lattice: Lattice[T], toaster: Toaster) {

  def render: VMod =
    editingValue.map(
      _.map(_ => renderEdit)
        .getOrElse(renderExistingValue),
    )

  def editingValue: Var[Option[T]] = value match {
    case Existing(value) => value.editingValue
    case New(value)      => value
  }

  def existingValue = value match {
    case Existing(value) => Some(value)
    case New(value)      => None
  }

  private def renderEdit: VMod = {
    val deleteModal = Var[Option[Modal]](None)
    tr(
      cls := "border-b dark:border-gray-700",
      data.id := existingValue.map(v => v.id),
      uiAttributes.map(ui => {
        ui.renderEdit(s"form-${existingValue.map(_.id)}", editingValue)
      }),
      td(
        cls := "border border-gray-300 py-1 w-1/6",
        div(
          cls := "h-full w-full flex flex-row items-center gap-2 justify-center px-4",
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
                  TableButton(LightButtonStyle.Primary)(
                    formId := s"form-${existingValue.map(_.id)}",
                    `type` := "submit",
                    idAttr := "add-entity-button",
                    "Save",
                  ),
                  TableButton(LightButtonStyle.Default)(
                    "Cancel",
                    onClick.foreach(_ => cancelEdit()),
                  ),
                )
              }
              case None => {
                TableButton(LightButtonStyle.Primary)(
                  // cls := "bg-purple-200 hover:bg-purple-300 text-purple-600 rounded px-2 py-0 h-fit uppercase font-bold",
                  formId := s"form-${existingValue.map(_.id)}",
                  `type` := "submit",
                  idAttr := "add-entity-button",
                  "Add Entity",
                )
              }
            }
          },
          existingValue.map(p => {
            val modal = new Modal(
              "Delete",
              s"Do you really want to delete the entity \"${p.signal.now.identifier.get.getOrElse("")}\"?",
              Seq(
                new ModalButton(
                  "Delete",
                  "bg-red-600",
                  () => {
                    removeEntity(p)
                    cancelEdit()
                  },
                ),
                new ModalButton("Cancel"),
              ),
            )
            deleteModal.set(Some(modal))
            val res = {
              IconButton(LightButtonStyle.Error)(
                Icons.close("fill-red-600 w-4 h-4"),
                cls := "tooltip tooltip-left",
                data.tip := "Delete",
                onClick.foreach(_ => modal.open()),
              )
            }
            res
          }),
        ),
      ), {
        deleteModal.map(_.map(_.render))
      },
    )
  }

  private def renderExistingValue: VMod = existingValue.map(renderSynced)

  private def renderSynced(synced: Synced[T]): VMod = {
    val modal = new Modal(
      "Delete",
      s"Do you really want to delete the entity \"${synced.signal.now.identifier.get.getOrElse("")}\"?",
      Seq(
        new ModalButton(
          "Delete",
          "bg-red-600",
          () => removeEntity(synced),
        ),
        new ModalButton("Cancel"),
      ),
    )
    synced.signal.map(p => {
      val res = if (p.exists.get.getOrElse(true)) {
        Some(
          tr(
            cls := "border border-gray-300 odd:bg-slate-50",
            data.id := synced.id,
            uiAttributes.map(ui => {
              ui.render(p)
            }),
            td(
              cls := "py-1 px-4 flex flex-row items-center gap-2 justify-center",
              TableButton(LightButtonStyle.Primary)(
                "Edit",
                onClick.foreach(_ => startEditing()),
              ),
              IconButton(LightButtonStyle.Error)(
                Icons.close("fill-red-600 w-4 h-4"),
                cls := "tooltip tooltip-top",
                data.tip := "Delete",
                onClick.foreach(_ => modal.open()),
              ),
            ),
            modal.render,
          ),
        )
      } else {
        None
      }
      res
    })
  }

  private def removeEntity(s: Synced[T]): Unit = {
    s.update(e => e.get.withExists(false))
      .toastOnError(ToastMode.Infinit)
  }

  private def cancelEdit(): Unit = {
    editingValue.set(None)
  }

  private def createOrUpdate(): Unit = {
    val editingNow = editingValue.now
    existingValue match {
      case Some(existing) => {
        existing
          .update(p => {
            p.get.merge(editingNow.get)
          })
          .toastOnError(ToastMode.Infinit)
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
          .toastOnError(ToastMode.Infinit)
      }
    }
  }

  private def startEditing(): Unit = {
    editingValue.set(Some(existingValue.get.signal.now))
  }
}

private class FilterRow[EntityType](uiAttributes: Seq[UIAttribute[EntityType, ? <: Any]]) {

  private val filters = uiAttributes.map(_.uiFilter)

  def render: VNode = tr(
    filters.map(_.render),
  )

  val predicate: Signal[EntityType => Boolean] = {
    val preds = filters.map(_.predicate).seqToSignal
    preds.map(preds => (e: EntityType) => preds.forall(_(e)))
  }

}

abstract class EntityPage[T <: Entity[T]](repository: Repository[T], uiAttributes: Seq[UIAttribute[T, ? <: Any]])(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    toaster: Toaster,
) extends Page {

  private val addEntityRow: EntityRow[T] =
    EntityRow[T](repository, New(Var(Some(bottom.empty.default))), uiAttributes)

  private val entityRows: Signal[Seq[EntityRow[T]]] =
    repository.all.map(
      _.map(syncedEntity => {
        EntityRow[T](repository, Existing(syncedEntity), uiAttributes)
      }),
    )

  private val filterRow = FilterRow[T](uiAttributes)

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    navigationHeader(
      div(
        cls := "relative overflow-x-visible shadow-md sm:rounded-lg p-4 m-4",
        table(
          cls := "border-collapse w-full text-left table-auto",
          thead(
            tr(
              uiAttributes.map(a =>
                th(
                  cls := "border-gray-300 border-b-2 border dark:border-gray-500 p-4 uppercase first-of-type:rounded-tl",
                  a.label,
                ),
              ),
              th(cls := "border-gray-300 border border-b-2 dark:border-gray-500 p-4 uppercase", "Actions"),
            ),
          ),
          tbody(
            // filterRow.render,
            renderEntities,
          ),
          tfoot(
            cls := "mt-2",
            addEntityRow.render,
          ),
        ),
      ),
    )
  }

  private def renderEntities = {
    filterRow.predicate
      .map(p =>
        entityRows.map(
          _.filterSignal(
            _.existingValue.mapToSignal(_.signal).map(_.exists(p)),
          )
            .mapInside(_.render),
        ),
      )
      .flatten
  }
}
