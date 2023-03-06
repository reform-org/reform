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
import ContractsPage.*
import webapp.npm.JSUtils.dateDiffHumanReadable
import webapp.npm.JSUtils.dateDiffMonth
import webapp.npm.JSUtils.toMoneyString

// TODO FIXME implement this using the proper existingValue=none, editingValue=Some logic
case class NewContractPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexeddb: IIndexedDB,
) extends Page {

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    div(
      repositories.contracts
        .create(Contract.empty.default)
        .map(currentContract => {
          InnerEditContractsPage(Some(currentContract), "").render()
        }),
    )
  }
}

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
              InnerEditContractsPage(Some(currentContract), contractId).render()
            case None =>
              navigationHeader(
                div(
                  div(
                    cls := "p-1",
                    h1(cls := "text-4xl text-center", "Contract"),
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

case class InnerEditContractsPage(existingValue: Option[Synced[Contract]], contractId: String)(using
    toaster: Toaster,
    repositories: Repositories,
    routing: RoutingService,
    indexeddb: IIndexedDB,
) {
  val startEditEntity: Option[Contract] = existingValue.map(_.signal.now)

  private def createOrUpdate(finalize: Boolean = false): Unit = {
    indexeddb.requestPersistentStorage

    val editingNow = editingValue.now.get._2.now
    existingValue match {
      case Some(existing) => {
        existing
          .update(p => {
            val c = p.getOrElse(Contract.empty).merge(editingNow)
            if (finalize) {
              c.copy(isDraft = c.isDraft.set(false))
            } else {
              c
            }
          })
          .map(value => {
            // editingValue.set(None)
            toaster.make(
              "Contract saved!",
              ToastMode.Short,
              ToastType.Success,
            )
            if (value.isDraft.get.getOrElse(true)) {
              routing.to(ContractDraftsPage())
            } else {
              routing.to(ContractsPage())
            }
          })
          .toastOnError(ToastMode.Infinit)
      }
      case None => {
        repositories.contracts
          .create({
            if (finalize) {
              editingNow.copy(isDraft = editingNow.isDraft.set(false))
            } else {
              editingNow
            }
          })
          .map(entity => {
            editingValue.set(Some((Contract.empty.default, Var(Contract.empty.default))))
            if (entity.signal.now.isDraft.get.getOrElse(true)) {
              routing.to(ContractDraftsPage())
            } else {
              routing.to(ContractsPage())
            }
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
      cls := "border rounded-2xl m-4 border-purple-200 dark:text-gray-200",
      div(
        cls := "bg-purple-200 p-4 rounded-t-2xl dark:text-gray-600",
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
          h1(
            cls := "text-3xl mt-4 text-center",
            Signal.dynamic {
              editingValue.resource.value.map((_, value) =>
                if (value.resource.value.isDraft.get.getOrElse(true)) "Edit Contract Draft" else "Edit Contract",
              )
            },
          ),
        ),
        div(
          cls := "relative shadow-md rounded-lg p-4 my-4 mx-[2.5%] inline-block overflow-y-visible w-[95%]",
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
                        dateDiffHumanReadable(
                          v.contractStartDate.get.getOrElse(0L),
                          v.contractEndDate.get.getOrElse(0L),
                        )
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
                    p(
                      cls := "bg-blue-100 dark:bg-blue-200 dark:text-blue-600",
                      "Monthly base salary: ",
                      Signal.dynamic {
                        editingValue.value match {
                          case None => "-"
                          case Some(c) => {
                            val contract = c(1).resource.value
                            contract.contractAssociatedPaymentLevel.get match {
                              case None => ""
                              case Some(contractAssociatedPaymentLevel) => {
                                val hourlyWage = getMoneyPerHour(
                                  contractAssociatedPaymentLevel,
                                  contract,
                                  contract.contractStartDate.get.getOrElse(0L),
                                ).value
                                s"${toMoneyString(contract.contractHoursPerMonth.get.getOrElse(0) * hourlyWage)} (calculating with a salary of ${toMoneyString(hourlyWage)}/h that was set when the contract has bin created)"
                              }
                            }

                          }
                        }
                      },
                    ),
                    br,
                    p(
                      cls := "bg-blue-100 dark:bg-blue-200 dark:text-blue-600",
                      "Total Hours: ",
                      Signal.dynamic {
                        editingValue.value match {
                          case None => "-"
                          case Some(c) => {
                            val contract = c(1).resource.value
                            val month = dateDiffMonth(
                              contract.contractStartDate.get.getOrElse(0L),
                              contract.contractEndDate.get.getOrElse(0L),
                            )
                            s"${contract.contractHoursPerMonth.get.getOrElse(0) * month} (calculating with ${month} month)"
                          }
                        }
                      },
                    ),
                  ),
                  div(
                    cls := "basis-1/2",
                    label(cls := "font-bold", "Payment Level:"),
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
                  cls := "basis-[12.5%] flex flex-col",
                  label(
                    cls := "font-bold",
                    Signal.dynamic {
                      editingValue.resource.value.map((_, value) =>
                        if (value.resource.value.isDraft.get.getOrElse(true)) "Contracts" else "Other Contracts",
                      )
                    },
                  ),
                  div(
                    Signal.dynamic {
                      editingValue.resource.value.flatMap((_, value) =>
                        value.resource.value.contractAssociatedProject.get.flatMap(id =>
                          repositories.projects.all.value
                            .find(project => project.id == id)
                            .map(project =>
                              s"${repositories.contracts.all.value
                                  .filter(c => c.id != contractId)
                                  .map(c => c.signal.value)
                                  .filter(c =>
                                    c.contractAssociatedProject.get.getOrElse("") == project.id && !c.isDraft.get
                                      .getOrElse(true),
                                  )
                                  .map(c =>
                                    c.contractHoursPerMonth.get.getOrElse(0) * dateDiffMonth(
                                      c.contractStartDate.get.getOrElse(0L),
                                      c.contractEndDate.get.getOrElse(0L),
                                    ),
                                  )
                                  .fold[Int](0)((a, b) => a + b)} h",
                            ),
                        ),
                      )
                    },
                  ),
                ),
                div(
                  cls := "basis-[12.5%] flex flex-col",
                  label(
                    cls := "font-bold",
                    Signal.dynamic {
                      editingValue.resource.value.map((_, value) =>
                        if (value.resource.value.isDraft.get.getOrElse(true)) "Other Drafts" else "Drafts",
                      )
                    },
                  ),
                  div(
                    Signal.dynamic {
                      editingValue.resource.value.flatMap((_, value) =>
                        value.resource.value.contractAssociatedProject.get.flatMap(id =>
                          repositories.projects.all.value
                            .find(project => project.id == id)
                            .map(project =>
                              s"${repositories.contracts.all.value
                                  .filter(c => c.id != contractId)
                                  .map(c => c.signal.value)
                                  .filter(c =>
                                    c.contractAssociatedProject.get.getOrElse("") == project.id && c.isDraft.get
                                      .getOrElse(true),
                                  )
                                  .map(c =>
                                    c.contractHoursPerMonth.get.getOrElse(0) * dateDiffMonth(
                                      c.contractStartDate.get.getOrElse(0L),
                                      c.contractEndDate.get.getOrElse(0L),
                                    ),
                                  )
                                  .fold[Int](0)((a, b) => a + b)} h",
                            ),
                        ),
                      )
                    },
                  ),
                ),
                div(
                  cls := "basis-[12.5%] flex flex-col",
                  label(
                    cls := "font-bold",
                    Signal.dynamic {
                      editingValue.resource.value.map((_, value) =>
                        if (value.resource.value.isDraft.get.getOrElse(true)) "This Draft" else "This Contract",
                      )
                    },
                  ),
                  div(
                    Signal.dynamic {
                      editingValue.resource.value.map((_, value) =>
                        s"${value.resource.value.contractHoursPerMonth.get.getOrElse(0) * dateDiffMonth(
                            value.resource.value.contractStartDate.get.getOrElse(0L),
                            value.resource.value.contractEndDate.get.getOrElse(0L),
                          )} h",
                      )
                    },
                  ),
                ),
                div(
                  cls := "basis-[12.5%] flex flex-col",
                  label(cls := "font-bold", "Max. hours"),
                  div(Signal.dynamic {
                    editingValue.resource.value.flatMap((_, value) =>
                      value.resource.value.contractAssociatedProject.get.flatMap(id =>
                        repositories.projects.all.value
                          .find(project => project.id == id)
                          .map(value => s"${value.signal.value.maxHours.get.getOrElse(0)} h"),
                      ),
                    )
                  }),
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
                // TODO only show documents that are included by contract schema
                Signal.dynamic {
                  editingValue.resource.value.map((_, contract) =>
                    contract.value.contractType.get.flatMap(contractTypeId =>
                      repositories.contractSchemas.all.value.find(contractType =>
                        contractType.id == contractTypeId,
                      ) match {
                        case None => Some(span("Please select a contract type"))
                        case Some(value) =>
                          value.signal.value.files.get.flatMap(files => {
                            val documents = repositories.requiredDocuments.all.value
                            val checkedDocuments = contract.value.requiredDocuments.get
                            Some(
                              div(
                                checkedDocuments
                                  .map(_ ++ files)
                                  .map(files =>
                                    div(
                                      files.toSet
                                        .map(file =>
                                          LabeledCheckbox(
                                            documents
                                              .find(doc => doc.id == file)
                                              .map(file => file.signal.value.name.get),
                                          )(
                                            CheckboxStyle.Default,
                                            checked := checkedDocuments.map(d => d.contains(file)),
                                          ),
                                        )
                                        .toSeq,
                                    ),
                                  ),
                              ),
                            )
                          })
                      },
                    ),
                  )

                },
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

                    editingValue.now match {
                      case None => {
                        toaster.make("No contract is being edited!", ToastMode.Long, ToastType.Error)
                        document.getElementById("loadPDF").classList.remove("loading")
                      }
                      case Some(editingValue) => {
                        val contract = editingValue._2.now
                        contract.contractAssociatedHiwi.get match {
                          case None => {
                            toaster.make("No HiWi associated with contract!", ToastMode.Long, ToastType.Error)
                            document.getElementById("loadPDF").classList.remove("loading")
                          }
                          case Some(hiwiId) => {
                            val hiwis = repositories.hiwis.all.now
                            val hiwi = hiwis.find(hiwi => hiwi.id == hiwiId)
                            hiwi match {
                              case None => {
                                toaster.make("This HiWi does not seem to exist!", ToastMode.Long, ToastType.Error)
                                document.getElementById("loadPDF").classList.remove("loading")
                              }
                              case Some(_hiwi) => {
                                val hiwi = _hiwi.signal.now
                                contract.contractAssociatedPaymentLevel.get match {
                                  case None => {
                                    toaster.make(
                                      "No payment level associated with contract!",
                                      ToastMode.Long,
                                      ToastType.Error,
                                    )
                                    document.getElementById("loadPDF").classList.remove("loading")
                                  }
                                  case Some(paymentLevelId) => {
                                    val paymentLevels = repositories.paymentLevels.all.now
                                    val paymentLevel =
                                      paymentLevels.find(paymentLevel => paymentLevel.id == paymentLevelId)
                                    paymentLevel match {
                                      case None => {
                                        toaster.make(
                                          "This payment level does not seem to exist!",
                                          ToastMode.Long,
                                          ToastType.Error,
                                        )
                                        document.getElementById("loadPDF").classList.remove("loading")
                                      }
                                      case Some(_paymentLevel) => {
                                        val paymentLevel = _paymentLevel.signal.now
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
                                                  contract.contractHoursPerMonth.get.getOrElse(0).toString,
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
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }),
                ),
                signed.renderEdit("", editingValue),
              ),
            ),
            editStep(
              "5",
              "Letter to Deanship",
              div(
                cls := "p-4",
                Button(
                  ButtonStyle.LightDefault,
                  "Create Letter",
                  idAttr := "loadLetter",
                  onClick.foreach(e => {
                    e.preventDefault()
                    document.getElementById("loadLetter").classList.add("loading")

                    editingValue.now match {
                      case None => {
                        toaster.make("No contract is being edited!", ToastMode.Long, ToastType.Error)
                        document.getElementById("loadLetter").classList.remove("loading")
                      }
                      case Some(editingValue) => {
                        val contract = editingValue._2.now
                        contract.contractAssociatedHiwi.get match {
                          case None => {
                            toaster.make("No HiWi associated with contract!", ToastMode.Long, ToastType.Error)
                            document.getElementById("loadLetter").classList.remove("loading")
                          }
                          case Some(hiwiId) => {
                            val hiwis = repositories.hiwis.all.now
                            val hiwi = hiwis.find(hiwi => hiwi.id == hiwiId)
                            hiwi match {
                              case None => {
                                toaster.make("This HiWi does not seem to exist!", ToastMode.Long, ToastType.Error)
                                document.getElementById("loadLetter").classList.remove("loading")
                              }
                              case Some(_hiwi) => {
                                val hiwi = _hiwi.signal.now
                                contract.contractAssociatedPaymentLevel.get match {
                                  case None => {
                                    toaster.make(
                                      "No payment level associated with contract!",
                                      ToastMode.Long,
                                      ToastType.Error,
                                    )
                                    document.getElementById("loadLetter").classList.remove("loading")
                                  }
                                  case Some(paymentLevelId) => {
                                    val paymentLevels = repositories.paymentLevels.all.now
                                    val paymentLevel =
                                      paymentLevels.find(paymentLevel => paymentLevel.id == paymentLevelId)
                                    paymentLevel match {
                                      case None => {
                                        toaster.make(
                                          "This payment level does not seem to exist!",
                                          ToastMode.Long,
                                          ToastType.Error,
                                        )
                                        document.getElementById("loadLetter").classList.remove("loading")
                                      }
                                      case Some(_paymentLevel) => {
                                        val paymentLevel = _paymentLevel.signal.now
                                        js.dynamicImport {
                                          PDF
                                            .fill(
                                              "/letter_editable.pdf",
                                              "letter.pdf",
                                              Seq(
                                                PDFTextField(
                                                  "Name VornameRow1",
                                                  s"${hiwi.lastName.get.getOrElse("")}, ${hiwi.firstName.get.getOrElse("")}",
                                                ),
                                                PDFTextField(
                                                  "GebDatumRow1",
                                                  s"${toGermanDate(hiwi.birthdate.get.getOrElse(0))}",
                                                ),
                                                PDFTextField(
                                                  "Vertrags beginnRow1",
                                                  toGermanDate(contract.contractStartDate.get.getOrElse(0)),
                                                ),
                                                PDFTextField(
                                                  "Vertrags endeRow1",
                                                  toGermanDate(contract.contractEndDate.get.getOrElse(0)),
                                                ),
                                                PDFTextField("€StdRow1", 0.toString),
                                                PDFTextField("Stunden gesamtRow1", 0.toString),
                                                PDFTextField("Stunden gesamtSumme", 0.toString),
                                                PDFTextField(
                                                  "Std MonatRow1",
                                                  contract.contractHoursPerMonth.get.getOrElse(0).toString,
                                                ),
                                                PDFTextField(
                                                  "Account",
                                                  contract.contractAssociatedProject.get.getOrElse(""),
                                                ),
                                                PDFTextField(
                                                  "Datum",
                                                  toGermanDate(js.Date.now().toLong),
                                                ),
                                              ),
                                            )
                                            .andThen(s => {
                                              console.log(s)
                                              document.getElementById("loadLetter").classList.remove("loading")
                                            })
                                            .toastOnError()
                                        }.toFuture
                                          .toastOnError()
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }),
                ),
                submitted.renderEdit("", editingValue),
              ),
            ),
            div(
              cls := "pl-8 space-x-4",
              Button(
                ButtonStyle.LightPrimary,
                "Save and return",
                onClick.foreach(e => {
                  e.preventDefault()
                  createOrUpdate()
                }),
              ),
              Button(
                ButtonStyle.LightPrimary,
                "Save and finalize",
                onClick.foreach(e => {
                  e.preventDefault()
                  createOrUpdate(true)
                }),
              ),
              Button(ButtonStyle.LightDefault, "Cancel", onClick.foreach(_ => cancelEdit())),
            ),
          ),
        ),
      ),
    )

}
