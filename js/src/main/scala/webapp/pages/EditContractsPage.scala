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

import outwatch.*
import outwatch.dsl.*
import rescala.default
import rescala.default.*
import webapp.components.navigationHeader
import webapp.services.Page
import webapp.entity.*
import webapp.{*, given}
import webapp.services.DiscoveryService
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.services.{ToastMode, Toaster}
import webapp.repo.Synced
import webapp.components.common.*
import webapp.utils.Futures.*
import webapp.services.ToastType
import scala.scalajs.js.Date
import webapp.npm.IIndexedDB
import webapp.components.icons
import scala.scalajs.js
import org.scalajs.dom.{console, document}
import webapp.npm.{PDF, PDFCheckboxField, PDFTextField}
import webapp.npm.JSUtils.toGermanDate
import scala.annotation.nowarn

case class EditContractsPage(contractId: String)(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexeddb: IIndexedDB,
) extends Page {

  private val existingValue = repositories.contracts.all.map(_.find(c => c.id == contractId))

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    div(
      existingValue
        .map(currentContract => {
          val result: VMod = currentContract match {
            case Some(currentContract) =>
              InnerEditContractsPage(Some(currentContract)).render()
            case None =>
              navigationHeader(
                div(
                  div(
                    cls := "p-1",
                    h1(cls := "text-4xl text-center", "EditContractsPage"),
                  ),
                  h2("Contract not found"),
                ),
              )
          }
          result
        }),
    )
  }
}

case class InnerEditContractsPage(existingValue: Option[Synced[Contract]])(using
    toaster: Toaster,
    repositories: Repositories,
    routing: RoutingService,
    indexeddb: IIndexedDB,
) {
  val startEditEntity = existingValue.map(_.signal.now)

  private def contractAssociatedProject(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.projects.all.map(_.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse("")))),
      )
      .withLabel("Project")
      .require
      .bindAsSelect(
        _.contractAssociatedProject,
        (p, a) => p.copy(contractAssociatedProject = a),
      )
  }

  private def contractAssociatedHiwi(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.hiwis.all.map(list =>
          list.map(value =>
            value.id -> value.signal.map(v => v.firstName.get.getOrElse("") + " " + v.lastName.get.getOrElse("")),
          ),
        ),
      )
      .withLabel("Associated Hiwi")
      .require
      .bindAsSelect(
        _.contractAssociatedHiwi,
        (p, a) => p.copy(contractAssociatedHiwi = a),
      )
  }

  private def contractAssociatedSupervisor(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        options = repositories.supervisors.all.map(list =>
          list.map(value =>
            value.id -> value.signal.map(v => v.firstName.get.getOrElse("") + " " + v.lastName.get.getOrElse("")),
          ),
        ),
      )
      .withLabel("Associated Supervisors")
      .require
      .bindAsSelect(
        _.contractAssociatedSupervisor,
        (p, a) => p.copy(contractAssociatedSupervisor = a),
      )
  }

  private def contractAssociatedType(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.contractSchemas.all.map(list =>
          list.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse(""))),
        ),
      )
      .withLabel("Contract Type")
      .require
      .bindAsSelect(
        _.contractType,
        (p, a) => p.copy(contractType = a),
      )
  }

  private val contractStartDate = UIAttributeBuilder.date
    .withLabel("Start Date")
    .require
    .bindAsDatePicker[Contract](
      _.contractStartDate,
      (h, a) => h.copy(contractStartDate = a),
    )

  private val contractEndDate = UIAttributeBuilder.date
    .withLabel("Start Date")
    .require
    .bindAsDatePicker[Contract](
      _.contractEndDate,
      (h, a) => h.copy(contractEndDate = a),
    )

  private val contractHoursPerMonth = UIAttributeBuilder.int
    .withLabel("Hours per Month")
    .require
    .bindAsNumber[Contract](
      _.contractHoursPerMonth,
      (h, a) => h.copy(contractHoursPerMonth = a),
    )

  private def contractAssociatedPaymentLevel(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.paymentLevels.all.map(list =>
          list.map(value => value.id -> value.signal.map(v => v.title.get.getOrElse(""))),
        ),
      )
      .withLabel("Associated PaymentLevel")
      .require
      .bindAsSelect(
        _.contractAssociatedPaymentLevel,
        (p, a) => p.copy(contractAssociatedPaymentLevel = a),
      )
  }

  private def createOrUpdate(): Unit = {
    indexeddb.requestPersistentStorage

    val editingNow = editingValue.now.get._2.now
    existingValue match {
      case Some(existing) => {
        existing
          .update(p => {
            p.getOrElse(Contract.empty).merge(editingNow)
          })
          .map(value => {
            editingValue.set(None)
            toaster.make(
              "Contract saved!",
              ToastMode.Short,
              ToastType.Success,
            )
          })
          .map(value => {
            routing.to(ContractsPage())
          })
          .toastOnError(ToastMode.Infinit)
      }
      case None => {
        repositories.contracts
          .create()
          .flatMap(entity => {
            editingValue.set(Some((Contract.empty.default, Var(Contract.empty.default))))
            //  TODO FIXME we probably should special case initialization and not use the event
            entity.update(p => {
              p.getOrElse(Contract.empty).merge(editingNow)
            })
          })
          .map(value => {
            routing.to(ContractsPage())
          })
          .toastOnError(ToastMode.Infinit)
      }
    }
  }

  private def cancelEdit(): Unit = {
    routing.to(ContractsPage())
  }

  private def editStep(number: String, title: String, props: VMod*): VNode = {
    div(
      cls := "border rounded-2xl m-4 border-purple-200",
      div(
        cls := "bg-purple-200 p-4 rounded-t-2xl",
        p("STEP " + number + ": " + title),
      ),
      props,
    )
  }

  var editingValue: Var[Option[(Contract, Var[Contract])]] = Var(
    Option(existingValue.get.signal.now, Var(existingValue.get.signal.now)),
  )

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode =
    navigationHeader(
      div(
        div(
          cls := "p-1",
          h1(cls := "text-3xl mt-4 text-center", "Edit Contract"),
        ),
        div(
          cls := "relative shadow-md rounded-lg p-4 my-4 mx-[2.5%] inline-block overflow-y-visible w-[95%]",
          p(cls := "pl-4", "Editing Contract: ", label(existingValue.map(p => p.id))),
          form(
            editStep(
              "1",
              "Basic information",
              div(
                cls := "p-4 space-y-4",
                div(
                  cls := "flex space-x-4",
                  div(
                    cls := "basis-1/2",
                    label(cls := "font-bold", "Hiwi:"),
                    contractAssociatedHiwi.renderEdit("", editingValue),
                    LabeledCheckbox("Hiwi has a degree", cls := "text-left")(CheckboxStyle.Default),
                  ),
                  div(
                    cls := "basis-1/2",
                    label(cls := "font-bold", "Supervisor:"),
                    contractAssociatedSupervisor.renderEdit("", editingValue),
                  ),
                ),
                div(
                  cls := "flex space-x-4",
                  div(
                    cls := "basis-2/5",
                    label(cls := "font-bold", "Start date:"),
                    contractStartDate.renderEdit("", editingValue),
                    editingValue.map(p =>
                      p.get._2.map(v => {
                        if (
                          v.contractStartDate.get.getOrElse(0L) - (Date
                            .now() / (1000 * 3600 * 24)).toLong < 0 && v.contractStartDate.get.getOrElse(0L) != 0
                        ) {
                          Some(
                            dsl.p(
                              cls := "bg-yellow-100 text-yellow-600 flex flex-row",
                              icons.WarningTriangle(cls := "w-6 h-6"),
                              "Start date is in the past",
                            ),
                          )
                        } else None
                      }),
                    ),
                  ),
                  div(
                    cls := "basis-1/5",
                    label(cls := "font-bold", "Duration: "),
                    br,
                    editingValue.map(p =>
                      p.get._2.map(v => {
                        if (v.contractEndDate.get.getOrElse(0L) - v.contractStartDate.get.getOrElse(0L) > 0)
                          (v.contractEndDate.get.getOrElse(0L) - v.contractStartDate.get
                            .getOrElse(0L)).toString() + " days"
                        else ""
                      }),
                    ),
                  ),
                  div(
                    cls := "basis-2/5",
                    label(cls := "font-bold", "End date:"),
                    contractEndDate.renderEdit("", editingValue),
                    editingValue.map(p =>
                      p.get._2.map(v => {
                        if (
                          v.contractEndDate.get.getOrElse(0L) - v.contractStartDate.get
                            .getOrElse(0L) < 0 && v.contractEndDate.get.getOrElse(0L) != 0
                        ) {
                          Some(
                            dsl.p(
                              cls := "bg-yellow-100 text-yellow-600 flex flex-row",
                              icons.WarningTriangle(cls := "w-6 h-6"),
                              "End date is in the past or before start date",
                            ),
                          )
                        } else None
                      }),
                    ),
                  ),
                  // Todo Warning
                ),
                div(
                  cls := "flex space-x-4",
                  div( // TODO calculation of monthly base salary and total hours
                    cls := "basis-1/2",
                    p(cls := "bg-blue-100", "Monthly base salary: 1.500€; with bonus: 1.800€"),
                    br,
                    p(cls := "bg-blue-100", "Total Hours: 160h"),
                  ),
                  div(
                    cls := "basis-1/2",
                    label(cls := "font-bold", "Payment level:"),
                    contractAssociatedPaymentLevel.renderEdit("", editingValue),
                    label(cls := "font-bold", "Hours per month:"),
                    contractHoursPerMonth.renderEdit("", editingValue),
                  ),
                ),
              ),
            ),

            // Select Project Field
            editStep(
              "1b",
              "Select project",
              div(
                cls := "flex p-4 space-x-4",
                div(
                  cls := "basis-1/2",
                  label(cls := "font-bold", "Project:"),
                  contractAssociatedProject.renderEdit("", editingValue),
                ),
                div(
                  cls := "basis-[12.5%]",
                  label(cls := "font-bold", "Contract"),
                ),
                div(
                  cls := "basis-[12.5%]",
                  label(cls := "font-bold", "Other drafts"),
                ),
                div(
                  cls := "basis-[12.5%]",
                  label(cls := "font-bold", "This draft"),
                ),
                div(
                  cls := "basis-[12.5%]",
                  label(cls := "font-bold", "Max. hours"),
                ),
              ),
            ),
            // Contract Type Field
            editStep(
              "2",
              "Contract type",
              div(
                cls := "p-4",
                // TODO active contract checking
                p("Hiwi did not have an active contract ..."),
                label(cls := "font-bold", "Contract type:"),
                contractAssociatedType.renderEdit("", editingValue),
              ),
            ),
            // Contract Requirements
            editStep(
              "3a",
              "Contract requirements - reminder mail",
              div(
                cls := "p-4",
                "Send a reminder e-mail to Hiwi",
              ),
            ),
            // Check requirements
            editStep(
              "3",
              "Check requirements",
              div(
                cls := "p-4",
                "Check all forms the hiwi has filled out and handed back.",
              ),
            ),
            editStep(
              "4",
              "Create Documents",
              div(
                cls := "p-4",
                Button(
                  ButtonStyle.LightDefault,
                  "Create Contract PDF",
                  idAttr := "loadPDF",
                  onClick.foreach(e => {
                    e.preventDefault()
                    document.getElementById("loadPDF").classList.add("loading")

                    existingValue match {
                      case None => {
                        toaster.make("No existing contract found!", ToastMode.Long, ToastType.Error)
                      }
                      case Some(existingValue) => {
                        val contract = existingValue.signal.now
                        contract.contractAssociatedHiwi.get match {
                          case None => {
                            toaster.make("No HiWi associated with contract!", ToastMode.Long, ToastType.Error)
                          }
                          case Some(hiwiId) => {
                            val hiwis = repositories.hiwis.all.now
                            val hiwi = hiwis.find(hiwi => hiwi.id == hiwiId)
                            hiwi match {
                              case None => { /* no hiwi with id*/ }
                              case Some(hiwi) => {
                                hiwi.signal.map(hiwi => {
                                  contract.contractAssociatedPaymentLevel.get match {
                                    case None => { /* no hiwi assigned */ }
                                    case Some(paymentLevelId) => {
                                      repositories.paymentLevels.all.map(paymentLevels => {
                                        val paymentLevel =
                                          paymentLevels.find(paymentLevel => paymentLevel.id == paymentLevelId)
                                        paymentLevel match {
                                          case None => { /* no paymentlevel with id */ }
                                          case Some(paymentLevel) => {
                                            paymentLevel.signal.map(paymentLevel => {
                                              js.dynamicImport {
                                                PDF
                                                  .fill(
                                                    "/contract_unlocked.pdf",
                                                    "arbeitsvertrag.pdf",
                                                    Seq(
                                                      PDFTextField(
                                                        "Vorname Nachname (Studentische Hilfskraft)",
                                                        s"${hiwi.firstName.get.getOrElse("")} ${hiwi.lastName.get.getOrElse("")}",
                                                      ),
                                                      PDFTextField(
                                                        "Geburtsdatum (Studentische Hilfskraft)",
                                                        s"${toGermanDate(hiwi.birthdate.get.getOrElse(0))}",
                                                      ),
                                                      PDFTextField(
                                                        "Vertragsbeginn",
                                                        toGermanDate(contract.contractStartDate.get.getOrElse(0)),
                                                      ),
                                                      PDFTextField(
                                                        "Vertragsende",
                                                        toGermanDate(contract.contractEndDate.get.getOrElse(0)),
                                                      ),
                                                      PDFTextField(
                                                        "Arbeitszeit Kästchen 1",
                                                        contract.contractHoursPerMonth.get.getOrElse(0).toString(),
                                                      ),
                                                      PDFCheckboxField("Arbeitszeit Kontrollkästchen 1", true),
                                                      PDFCheckboxField(
                                                        paymentLevel.pdfCheckboxName.get.getOrElse(""),
                                                        true,
                                                      ),
                                                    ),
                                                  )
                                                  .andThen(s => {
                                                    console.log(s)
                                                    document.getElementById("loadPDF").classList.remove("loading")
                                                  })
                                                  .toastOnError()
                                              }.toFuture
                                                .toastOnError()
                                            })
                                          }
                                        }
                                      })
                                    }
                                  }
                                })
                              }
                            }

                          }
                        }

                      }
                    }: @nowarn
                  }),
                ),
              ),
            ),
            div(
              cls := "pl-8 space-x-4",
              Button(
                ButtonStyle.LightPrimary,
                "Save",
                onClick.foreach(e => {
                  e.preventDefault()
                  createOrUpdate()
                }),
              ),
              Button(ButtonStyle.LightDefault, "Cancel", onClick.foreach(_ => cancelEdit())),
            ),
          ),
        ),
      ),
    )

}
