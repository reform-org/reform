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

import scala.collection.mutable
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
import webapp.npm.JSUtils.createPopper

import scala.collection.mutable

sealed trait EntityValue[T]
case class Existing[T](value: Synced[T], editingValue: Var[Option[(T, Var[T])]] = Var[Option[(T, Var[T])]](None))
    extends EntityValue[T]

// TODO FIXME the type here is unecessarily complex because the Var types need to be the same
case class New[T](value: Var[Option[(T, Var[T])]]) extends EntityValue[T]

abstract class EntityRowBuilder[T <: Entity[T]] {
  def construct(
      repository: Repository[T],
      value: EntityValue[T],
      uiAttributes: Seq[UIBasicAttribute[T]],
  )(using
      bottom: Bottom[T],
      lattice: Lattice[T],
      toaster: Toaster,
      routing: RoutingService,
      repositories: Repositories,
  ): EntityRow[T]
}

class DefaultEntityRow[T <: Entity[T]] extends EntityRowBuilder[T] {
  def construct(repository: Repository[T], value: EntityValue[T], uiAttributes: Seq[UIBasicAttribute[T]])(using
      bottom: Bottom[T],
      lattice: Lattice[T],
      toaster: Toaster,
      routing: RoutingService,
      repositories: Repositories,
  ): EntityRow[T] = EntityRow[T](repository, value, uiAttributes)
}

class EntityRow[T <: Entity[T]](
    val repository: Repository[T],
    val value: EntityValue[T],
    val uiAttributes: Seq[UIBasicAttribute[T]],
)(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    toaster: Toaster,
    routing: RoutingService,
    repositories: Repositories,
) {

  def render: VMod =
    editingValue.map(
      _.map(_ => renderEdit)
        .getOrElse(renderExistingValue),
    )

  def editingValue: Var[Option[(T, Var[T])]] = value match {
    case Existing(_, editingValue) => editingValue
    case New(value)                => value
  }

  def existingValue: Option[Synced[T]] = value match {
    case Existing(value, _) => Some(value)
    case New(_)             => None
  }

  private def renderEdit: VMod = {
    val deleteModal = Var[Option[Modal]](None)
    val id = s"form-${existingValue.map(_.id).getOrElse("new")}"
    tr(
      cls := "",
      data.id := existingValue.map(v => v.id),
      key := existingValue.map(v => v.id).getOrElse("new"),
      uiAttributes.map(ui => {
        td(cls := "p-0", ui.renderEdit(id, editingValue))
      }),
      td(
        cls := "py-1 min-w-[185px] max-w-[185px] mx-auto sticky right-0 bg-white border-x border-b border-gray-300 !z-[1]",
        div(
          cls := "h-full w-full flex flex-row items-center gap-2 justify-center px-4",
          form(
            idAttr := id,
            onSubmit.foreach(e => {
              e.preventDefault()
              createOrUpdate()
            }),
          ), {
            existingValue match {
              case Some(p) => {
                List(
                  TableButton(
                    ButtonStyle.LightPrimary,
                    formId := id,
                    `type` := "submit",
                    idAttr := "add-entity-button",
                    "Save",
                  ),
                  TableButton(ButtonStyle.LightDefault, "Cancel", onClick.foreach(_ => cancelEdit())),
                )
              }
              case None => {
                TableButton(
                  ButtonStyle.LightPrimary,
                  formId := id,
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
              IconButton(
                ButtonStyle.LightError,
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
      span(
        "Do you really want to delete the entity \"",
        synced.signal.map(value => value.identifier.get.getOrElse("")),
        "\"?",
      ),
      Seq(
        new ModalButton(
          "Delete",
          "bg-red-600",
          () => removeEntity(synced),
        ),
        new ModalButton("Cancel"),
      ),
    )
    synced.signal.map[VMod](p => {
      if (p.exists) {
        tr(
          onDblClick.foreach(e => startEditing()),
          cls := "odd:bg-slate-50",
          data.id := synced.id,
          key := synced.id,
          uiAttributes.map(ui => {
            td(
              cls := "border-b border-l border-gray-300 p-0",
              cls := {
                ui.width match {
                  case None    => "min-w-[200px]"
                  case Some(v) => s"max-w-[$v] min-w-[$v]"
                }
              },
              ui.render(synced.id, p),
            )
          }),
          td(
            cls := "min-w-[185px] max-w-[185px] sticky right-0 bg-white border-x border-b border-gray-300 !z-[1]",
            div(
              cls := "h-full w-full flex flex-row items-center gap-2 justify-center px-4",
              TableButton(ButtonStyle.LightPrimary, "Edit", onClick.foreach(_ => startEditing())),
              IconButton(
                ButtonStyle.LightError,
                Icons.close("fill-red-600 w-4 h-4"),
                cls := "tooltip tooltip-top",
                data.tip := "Delete",
                onClick.foreach(_ => modal.open()),
              ),
            ),
          ),
          modal.render,
        )
      } else {
        None
      }
    })
  }

  private def removeEntity(s: Synced[T]): Unit = {
    s.update(e => e.get.withExists(false))
      .toastOnError(ToastMode.Infinit)
  }

  private def cancelEdit(): Unit = {
    editingValue.set(None)
  }

  protected def afterCreated(id: String): Unit = {}

  private def createOrUpdate(): Unit = {
    val editingNow = editingValue.now.get._2.now
    existingValue match {
      case Some(existing) => {
        existing
          .update(p => {
            p.get.merge(editingNow)
          })
          .toastOnError(ToastMode.Infinit)
        editingValue.set(None)
      }
      case None => {
        repository
          .create()
          .flatMap(entity => {
            editingValue.set(Some((bottom.empty.default, Var(bottom.empty.default))))
            entity
              .update(p => {
                p.getOrElse(bottom.empty).merge(editingNow)
              })
              .map(_ => entity)
          })
          .map(value => { afterCreated(value.id); value })
          .toastOnError(ToastMode.Infinit)
      }
    }
  }

  protected def startEditing(): Unit = {
    editingValue.set(Some((existingValue.get.signal.now, Var(existingValue.get.signal.now))))
  }
}

private class Filter[EntityType](uiAttributes: Seq[UIBasicAttribute[EntityType]]) {

  private val filters = uiAttributes.map(_.uiFilter)

  def render: VNode = tr(
    filters.map(_.render),
  )

  val predicate: Signal[EntityType => Boolean] = {
    val preds = filters.map(_.predicate).seqToSignal
    preds.map(preds => (e: EntityType) => preds.forall(_(e)))
  }

}

abstract class EntityPage[T <: Entity[T]](
    title: String,
    repository: Repository[T],
    uiAttributes: Seq[UIBasicAttribute[T]],
    entityRowContructor: EntityRowBuilder[T],
)(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    toaster: Toaster,
    routing: RoutingService,
    repositories: Repositories,
) extends Page {

  private val addEntityRow: EntityRow[T] =
    entityRowContructor.construct(
      repository,
      New(Var(Some((bottom.empty.default, Var(bottom.empty.default))))),
      uiAttributes,
    )

  private val cachedExisting: mutable.Map[String, Existing[T]] = mutable.Map.empty

  private val entityRows: Signal[Seq[EntityRow[T]]] =
    repository.all.map(
      _.map(syncedEntity => {
        val existing = cachedExisting.getOrElseUpdate(syncedEntity.id, Existing[T](syncedEntity))
        entityRowContructor.construct(repository, existing, uiAttributes)
      }),
    )

  private val filter = Filter[T](uiAttributes)

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    val filterDropdownOpen = Var(false)

    createPopper(s"#filter-btn", s"#filter-dropdown", "bottom-start")

    navigationHeader(
      div(
        h1(cls := "text-3xl mt-4 text-center", title),
        div(
          cls := "relative shadow-md rounded-lg p-4 my-4 mx-[2.5%] inline-block overflow-y-visible w-[95%]",
          div(
            cls := "flex flex-row gap-2 items-center mb-4",
            div(
              cls := "dropdown",
              cls <-- filterDropdownOpen.map(if (_) Some("dropdown-open") else None),
              Button(
                ButtonStyle.LightDefault,
                tabIndex := 0,
                "Filter",
                idAttr := "filter-btn",
                div(cls := "ml-3 badge", "0"),
                Icons.filter("ml-1 w-6 h-6", "#49556a"),
                cls := "!mt-0",
                onClick.foreach(e => {
                  filterDropdownOpen.transform(!_)
                }),
              ),
              ul(
                idAttr := "filter-dropdown",
                cls := "dropdown-content menu p-2 shadow-xl bg-base-100 rounded-box w-96",
                filter.render,
              ),
            ),
            div(
              renderEntities.flatten.map(filtered => filtered.length),
              " / ",
              entityRows
                .map(entityRows => entityRows.length),
              " Entities",
            ),
          ),
          div(
            cls := "overflow-x-auto custom-scrollbar",
            table(
              cls := "w-full text-left table-auto border-separate border-spacing-0 table-fixed-height mb-2",
              thead(
                tr(
                  uiAttributes.map(a =>
                    th(
                      cls := "border-gray-300 border-b-2 border-t border-l dark:border-gray-500 px-4 py-2 uppercase",
                      a.label,
                    ),
                  ),
                  th(
                    cls := "border-gray-300 border border-b-2 dark:border-gray-500 px-4 py-2 uppercase text-center sticky right-0 bg-white min-w-[185px] max-w-[185px] !z-[1]",
                    "Actions",
                  ),
                ),
              ),
              tbody(
                renderEntities,
              ),
              tfoot(
                tr(
                  cls := "h-4",
                ),
                cls := "",
                addEntityRow.render,
              ),
            ),
          ),
        ),
      ),
    )
  }

  private def renderEntities = {
    filter.predicate
      .map(pred =>
        entityRows.map(
          _.filterSignal(_.value match {
            case New(_)             => Signal(false)
            case Existing(value, _) => value.signal.map(pred)
          })
            .mapInside(_.render),
        ),
      )
      .flatten
  }
}
