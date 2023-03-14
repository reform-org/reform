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
import webapp.components.icons
import webapp.given_ExecutionContext
import webapp.npm.JSUtils.createPopper

import scala.collection.mutable
import webapp.npm.IIndexedDB
import scala.annotation.nowarn
import webapp.npm.JSUtils.downloadFile
import webapp.services.MailService

case class Title(singular: String) {

  val plural: String = singular + "s"

  def ofCardinal(card: Int): String =
    if (card == 1) singular
    else plural
}

sealed trait EntityValue[T]
case class Existing[T](value: Synced[T], editingValue: Var[Option[(T, Var[T])]] = Var[Option[(T, Var[T])]](None))
    extends EntityValue[T]

// TODO FIXME the type here is unnecessarily complex because the Var types need to be the same
case class New[T](value: Var[Option[(T, Var[T])]]) extends EntityValue[T]

abstract class EntityRowBuilder[T <: Entity[T]] {
  def construct(
      title: Title,
      repository: Repository[T],
      value: EntityValue[T],
      uiAttributes: Seq[UIBasicAttribute[T]],
  )(using
      bottom: Bottom[T],
      lattice: Lattice[T],
      jsImplicits: JSImplicits,
  ): EntityRow[T]
}

class DefaultEntityRow[T <: Entity[T]] extends EntityRowBuilder[T] {
  def construct(title: Title, repository: Repository[T], value: EntityValue[T], uiAttributes: Seq[UIBasicAttribute[T]])(
      using
      bottom: Bottom[T],
      lattice: Lattice[T],
      jsImplicits: JSImplicits,
  ): EntityRow[T] = EntityRow[T](title, repository, value, uiAttributes)
}

class EntityRow[T <: Entity[T]](
    val title: Title,
    val repository: Repository[T],
    val value: EntityValue[T],
    val uiAttributes: Seq[UIBasicAttribute[T]],
)(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    jsImplicits: JSImplicits,
) {

  def render: VMod = Signal {
    editingValue.value.map(_ => renderEdit).getOrElse(renderExistingValue)
  }

  def editingValue: Var[Option[(T, Var[T])]] = value match {
    case Existing(_, editingValue) => editingValue
    case New(value)                => value
  }

  def existingValue: Option[Synced[T]] = value match {
    case Existing(value, _) => Some(value)
    case New(_)             => None
  }

  protected val editLabel = "Edit"

  private def renderEdit: VMod = {
    val deleteModal = Var[Option[Modal]](None)
    val id = s"form-${existingValue.map(_.id).getOrElse("new")}"
    tr(
      cls := "odd:bg-slate-50 odd:dark:bg-gray-600",
      data.id := existingValue.map(v => v.id),
      key := existingValue.map(v => v.id).getOrElse("new"),
      Signal {
        val columns = jsImplicits.routing.getQueryParameterAsSeq("columns").value
        uiAttributes
          .filter(attr => columns.isEmpty || columns.contains(toQueryParameterName(attr.label)))
          .map(ui => {
            td(
              cls := "p-0 border-none",
              styleAttr := (ui.width match {
                case None    => "min-width: 200px"
                case Some(v) => s"max-width: $v; min-width: $v; width: $v"
              }),
              ui.renderEdit(id, editingValue),
            )
          })
      },
      td(
        cls := "py min-w-[130px] max-w-[130px] md:min-w-[185px] md:max-w-[185px] mx-auto sticky right-0 bg-white dark:bg-gray-600 border-x border-b border-gray-300 dark:border-gray-600 !z-[1]",
        div(
          cls := "h-full w-full flex flex-row items-center justify-center md:px-4",
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
                    icons.Save(cls := "w-4 h-4 md:hidden"),
                    span(cls := "hidden md:block", "Save"),
                    cls := "h-7 tooltip tooltip-top entity-save px-2",
                    data.tip := "Save",
                  ),
                  TableButton(
                    ButtonStyle.LightDefault,
                    icons.Close(cls := "w-4 h-4 md:hidden"),
                    span(cls := "hidden md:block", "Cancel"),
                    onClick.foreach(_ => cancelEdit()),
                    cls := "h-7 tooltip tooltip-top entity-cancel px-2",
                    data.tip := "Cancel",
                  ),
                )
              }
              case None => {
                TableButton(
                  ButtonStyle.LightPrimary,
                  formId := id,
                  `type` := "submit",
                  idAttr := "add-entity-button",
                  icons.Add(cls := "w-4 h-4 md:hidden"),
                  span(cls := "hidden md:block whitespace-nowrap", "Add " + this.title.singular),
                  cls := "h-7 tooltip tooltip-top entity-add",
                  data.tip := "Add" + this.title.singular,
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
                  ButtonStyle.Error,
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
              TableButton(
                ButtonStyle.LightError,
                icons.Delete(cls := "text-red-600 w-4 h-4"),
                cls := "h-7 tooltip tooltip-top",
                data.tip := "Delete",
                onClick.foreach(_ => modal.open()),
              )
            }
            res
          }),
        ),
      ),
      Signal {
        deleteModal.value.map(_.render)
      },
    )
  }

  private def renderExistingValue: VMod = existingValue.map(renderSynced)

  private def renderSynced(synced: Synced[T]): VMod = {
    val modal = new Modal(
      "Delete",
      span(
        s"Do you really want to delete the ${title.singular} \"",
        Signal { synced.signal.value.identifier.get.getOrElse("") },
        "\"?",
      ),
      Seq(
        new ModalButton(
          "Delete",
          ButtonStyle.Error,
          () => removeEntity(synced),
        ),
        new ModalButton("Cancel"),
      ),
    )
    synced.signal.map[VMod](p => {
      if (p.exists) {
        tr(
          onDblClick.foreach(e => startEditing()),
          cls := "odd:bg-slate-50 odd:dark:bg-gray-600",
          data.id := synced.id,
          key := synced.id,
          Signal {
            val columns = jsImplicits.routing.getQueryParameterAsSeq("columns").value
            uiAttributes
              .filter(attr => columns.isEmpty || columns.contains(toQueryParameterName(attr.label)))
              .map(ui => {
                td(
                  cls := "border-b border-l border-gray-300 dark:border-gray-700 p-0",
                  styleAttr := (ui.width match {
                    case None    => "min-width: 200px"
                    case Some(v) => s"max-width: $v; min-width: $v; width: $v"
                  }),
                  ui.render(synced.id, p),
                )
              })
          },
          td(
            cls := "min-w-[130px] max-w-[130px] md:min-w-[185px] md:max-w-[185px] sticky right-0 bg-white dark:bg-gray-600 border-l border-r border-b border-gray-300 dark:border-gray-700 odd:dark:bg-gray-600 !z-[1]",
            div(
              cls := "h-full w-full flex flex-row items-center gap-2 justify-center px-4",
              TableButton(
                ButtonStyle.LightPrimary,
                icons.Edit(cls := "w-4 h-4 md:hidden"),
                span(cls := "hidden md:block", editLabel),
                cls := "h-7 tooltip tooltip-top entity-edit",
                data.tip := editLabel,
                onClick.foreach(_ => startEditing()),
              ),
              TableButton(
                ButtonStyle.LightError,
                icons.Delete(cls := "text-red-600 w-4 h-4"),
                cls := "h-7 tooltip tooltip-top entity-delete",
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
    jsImplicits.indexeddb.requestPersistentStorage

    s.update(e => e.get.withExists(false))
      .toastOnError(ToastMode.Infinit)
  }

  private def cancelEdit(): Unit = {
    editingValue.set(None)
  }

  protected def afterCreated(id: String): Unit = {}

  private def createOrUpdate(): Unit = {
    jsImplicits.indexeddb.requestPersistentStorage

    val editingNow = editingValue.now.get._2.now
    existingValue match {
      case Some(existing) => {
        existing
          .update(p => {
            p.get.merge(editingNow)
          })
          .map(_ => {
            editingValue.set(None)
          })
          .toastOnError(ToastMode.Infinit)
      }
      case None => {
        repository
          .create(editingNow)
          .map(entity => {
            editingValue.set(Some((bottom.empty.default, Var(bottom.empty.default))))
            entity
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

  def render: VMod = filters.map(_.render)

  val predicate: Signal[EntityType => Boolean] = Signal.dynamic {
    val preds = filters.map(_.predicate.value)
    (e: EntityType) => preds.forall(_(e))
  }

}

abstract class EntityPage[T <: Entity[T]](
    title: Title,
    description: Option[VMod],
    repository: Repository[T],
    all: Signal[Seq[Synced[T]]],
    uiAttributes: Seq[UIBasicAttribute[T]],
    entityRowConstructor: EntityRowBuilder[T],
    addInPlace: Boolean = false,
    addButton: VMod = span(),
)(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    jsImplicits: JSImplicits,
) extends Page {

  private val addEntityRow: EntityRow[T] =
    entityRowConstructor.construct(
      title,
      repository,
      New(Var(Some((bottom.empty.default, Var(bottom.empty.default))))),
      uiAttributes,
    )

  private val cachedExisting: mutable.Map[String, Existing[T]] = mutable.Map.empty

  private val entityRows: Signal[Seq[EntityRow[T]]] =
    all
      .flatMap(
        _.sortBySignal(_.signal.map(_.identifier.get)),
      )
      .mapInside(syncedEntity => {
        val existing = cachedExisting.getOrElseUpdate(syncedEntity.id, Existing[T](syncedEntity))
        entityRowConstructor.construct(title, repository, existing, uiAttributes)
      })

  private val filter = Filter[T](uiAttributes)

  def render: VNode = {
    val filterDropdownClosed = Var(true)

    navigationHeader(
      div(
        h1(cls := "text-3xl mt-4 text-center", title.plural),
        div(
          cls := "w-[95%] mx-[2.5%] text-slate-400 dark:text-gray-200",
          description,
        ),
        div(
          cls := "relative shadow-md rounded-lg p-4 my-4 mx-[2.5%] inline-block overflow-y-visible w-[95%] dark:bg-gray-600",
          div(
            cls := "flex flex-col sm:flex-row gap-2 items-left md:items-center mb-4",
            div(
              cls := "",
              Button(
                ButtonStyle.LightDefault,
                tabIndex := 0,
                "Filter",
                idAttr := "filter-btn",
                div(
                  cls := "ml-3 badge",
                  jsImplicits.routing.countQueryParameters(
                    uiAttributes.map(attr => toQueryParameterName(attr.label)) :+ "columns",
                  ),
                ),
                icons.Filter(cls := "ml-1 w-6 h-6"),
                cls := "!mt-0",
                onClick.foreach(e => {
                  filterDropdownClosed.transform(!_)
                }),
              ),
            ),
            div(
              Signal.dynamic {
                s"${countFilteredEntities.value} / ${countEntities.value} ${title.plural}"
              },
            ),
            Button(
              ButtonStyle.LightDefault,
              "Export to Spreadsheet Editor",
              cls := "!m-0",
              onClick.foreach(_ => exportView()),
            ),
            if (addInPlace) {
              Some(addButton)
            } else None,
          ),
          div(
            cls <-- Signal { if (filterDropdownClosed.value) Some("hidden") else None },
            idAttr := "filter-dropdown",
            cls := "menu p-2 mb-4 bg-base-100 dark:bg-gray-700 transition-none flex flex-row gap-2",
            filter.render,
            div(
              cls := "max-w-[300px] min-w-[300px] min-w-[300px]",
              "Columns",
              MultiSelect(
                Signal(
                  uiAttributes.map(attr => SelectOption(toQueryParameterName(attr.label), Signal(attr.label))),
                ),
                value => jsImplicits.routing.updateQueryParameters(Map("columns" -> value)),
                jsImplicits.routing.getQueryParameterAsSeq("columns"),
                4,
                true,
                span("Nothing found..."),
                false,
                cls := "rounded-md min-w-full",
              ),
            ),
          ),
          div(
            cls := "overflow-x-auto custom-scrollbar",
            table(
              cls := "w-full text-left table-auto border-separate border-spacing-0 table-fixed-height mb-2",
              thead(
                tr(
                  Signal {
                    val columns = jsImplicits.routing.getQueryParameterAsSeq("columns").value
                    uiAttributes
                      .filter(attr => columns.isEmpty || columns.contains(toQueryParameterName(attr.label)))
                      .map(a =>
                        th(
                          cls := "border-gray-300 dark:border-gray-700 border-b-2 border-t border-l dark:border-gray-700 px-4 py-2 uppercase dark:bg-gray-600",
                          styleAttr := (a.width match {
                            case None    => "min-width: 200px"
                            case Some(v) => s"max-width: $v; min-width: $v; width: $v"
                          }),
                          a.label,
                        ),
                      )
                  },
                  th(
                    cls := "border-gray-300 dark:border-gray-700 border border-b-2 dark:border-gray-500 dark:bg-gray-600 px-4 py-2 uppercase text-center sticky right-0 bg-white min-w-[130px] max-w-[130px] md:min-w-[185px] md:max-w-[185px] !z-[1]",
                  ),
                ),
              ),
              tbody(
                cls := "dark:bg-gray-600",
                Signal {
                  if (countEntities.value == 0)
                    List(
                      tr(
                        cls := "h-4",
                      ),
                      tr(
                        td(
                          colSpan := 100,
                          cls := "text-slate-500",
                          "No entries.",
                        ),
                      ),
                    )
                  else if (countFilteredEntities.value == 0 && countEntities.value > 0)
                    List(
                      tr(
                        cls := "h-4",
                      ),
                      tr(
                        td(
                          colSpan := 100,
                          cls := "text-slate-500",
                          "No results for your filter.",
                        ),
                      ),
                    )
                  else List()
                },
                renderEntities,
              ),
              if (!addInPlace) {
                Some(
                  tfoot(
                    tr(
                      cls := "h-4",
                    ),
                    cls := "",
                    addEntityRow.render,
                  ),
                )
              } else None,
            ),
          ),
        ),
      ),
    )
  }

  private def countEntities: Signal[Int] = Signal.dynamic {
    entityRows.value
      .filter(_.value match {
        case New(_)             => false
        case Existing(value, _) => value.signal.value.exists
      })
      .size
  }

  private def countFilteredEntities: Signal[Int] = Signal.dynamic {
    val predicate = filter.predicate.value
    entityRows.value
      .filter(_.value match {
        case New(_)             => false
        case Existing(value, _) => value.signal.value.exists && predicate(value.signal.value)
      })
      .size
  }

  private def exportView(): Unit = Signal.dynamic {
    var csvHeader: Seq[String] = Seq()
    var csvData: Seq[String] = Seq()

    val columns = jsImplicits.routing.getQueryParameterAsSeq("columns").value
    val pred = filter.predicate.value
    val rows = entityRows.value.filter(_.value match {
      case New(_)             => false
      case Existing(value, _) => pred(value.signal.value)
    })

    rows.foreach(row => {
      row.value match {
        case New(_) => {}
        case Existing(v, _) =>
          val id = v.id
          val value = v.signal.value
          var csvRow: Seq[String] = Seq()
          var selectedHeaders: Seq[String] = Seq()
          row.uiAttributes
            .filter(attr => columns.isEmpty || columns.contains(toQueryParameterName(attr.label)))
            .foreach(attr => {
              selectedHeaders = selectedHeaders :+ attr.label
              attr match {
                case attr: UIReadOnlyAttribute[?, ?] => {
                  if (value.exists) {
                    val a = attr.getter(id, value).value
                    csvRow = csvRow :+ attr.readConverter(a)
                  }
                }
                case attr: UISelectAttribute[?, ?] => {
                  val a = attr.getter(value)
                  csvRow = csvRow :+ a.getAll
                    .map(x => attr.options(value).now.filter(p => p.id == x).map(v => v.name.value).mkString(", "))
                    .mkString(", ")
                }
                case attr: UIAttribute[?, ?] => {
                  if (value.exists) {
                    val a = attr.getter(value)
                    csvRow = csvRow :+ a.getAll.map(x => attr.readConverter(x)).mkString(", ")
                  }
                }
                case _ => {}
              }
            })

          if (csvHeader.isEmpty) csvHeader = selectedHeaders
          if (csvRow.nonEmpty)
            csvData = csvData :+ csvRow.map(escapeCSVString).mkString(",")
      }
    })

    val csvString = csvHeader.map(escapeCSVString).mkString(",") + "\n" + csvData.mkString("\n")
    downloadFile(title.plural + ".csv", csvString, "data:text/csv")
  }

  private def renderEntities = Signal.dynamic {
    val pred = filter.predicate.value
    entityRows.value
      .filter(_.value match {
        case New(_)             => false
        case Existing(value, _) => pred(value.signal.value)
      })
      .map(_.render)
  }
}
