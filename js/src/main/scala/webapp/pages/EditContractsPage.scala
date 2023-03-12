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
import webapp.npm.JSUtils.stickyButton
import org.scalajs.dom.KeyboardEvent
import scala.math.BigDecimal.RoundingMode
import scala.concurrent.Promise
import scala.concurrent.Future
import webapp.services.MailService
import webapp.JSImplicits

// TODO FIXME implement this using the proper existingValue=none, editingValue=Some logic
case class NewContractPage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render: VNode = {
    div(
      jsImplicits.repositories.contracts
        .create(Contract.empty.default)
        .map(currentContract => {
          InnerEditContractsPage(Some(currentContract), "").render()
        }),
    )
  }
}

case class EditContractsPage(contractId: String)(using
    jsImplicits: JSImplicits,
) extends Page {

  private val existingValue = jsImplicits.repositories.contracts.all.map(_.find(c => c.id == contractId))

  def render: VNode = {
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
    jsImplicits: JSImplicits,
) {
  val startEditEntity: Option[Contract] = existingValue.map(_.signal.now)

  private def createOrUpdate(finalize: Boolean = false, stayOnPage: Boolean = false): Future[String] = {
    jsImplicits.indexeddb.requestPersistentStorage

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
            jsImplicits.toaster.make(
              "Contract saved!",
              ToastMode.Short,
              ToastType.Success,
            )
            if (!stayOnPage) {
              if (value.isDraft.get.getOrElse(true)) {
                jsImplicits.routing.to(ContractDraftsPage())
              } else {
                jsImplicits.routing.to(ContractsPage())
              }
            }

            existing.id
          })
      }
      case None => {
        jsImplicits.repositories.contracts
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
              jsImplicits.routing.to(ContractDraftsPage())
            } else {
              jsImplicits.routing.to(ContractsPage())
            }

            entity.id
          })
      }
    }
  }

  private def cancelEdit(): Unit = {
    jsImplicits.routing.to(ContractsPage())
  }

  private def editStep(number: String, title: String, props: VMod*): VNode = {
    div(
      cls := "border rounded-2xl m-4 border-purple-200 dark:border-gray-500 dark:text-gray-200",
      div(
        cls := "bg-purple-200 p-4 rounded-t-2xl dark:bg-gray-700 dark:text-gray-200",
        p("STEP " + number + ": " + title),
      ),
      props,
    )
  }

  var editingValue: Var[Option[(Contract, Var[Contract])]] = Var(
    Option(existingValue.get.signal.now, Var(existingValue.get.signal.now)),
  )

  val actions = Seq(
    Button(
      ButtonStyle.LightPrimary,
      "Save",
      Signal.dynamic {
        if (
          existingValue.flatMap(existingValue =>
            editingValue.value.map((_, a) => a.value == existingValue.signal.value),
          ) == Some(false)
        ) span(cls := "inline-block ml-1", icons.Circle(cls := "w-3 h-3"))
        else span(cls := "inline-block ml-1", icons.Check(cls := "w-3 h-3"))
      },
      onClick.foreach(e => {
        e.preventDefault()
        createOrUpdate(false, true)
          .map(id => jsImplicits.routing.to(EditContractsPage(id), false, Map.empty, true))
          .toastOnError(ToastMode.Infinit)
      }),
    ),
    Button(
      ButtonStyle.LightPrimary,
      "Save and return",
      onClick.foreach(e => {
        e.preventDefault()
        createOrUpdate().toastOnError(ToastMode.Infinit)
      }),
    ),
    Button(
      ButtonStyle.LightPrimary,
      "Save and finalize",
      onClick.foreach(e => {
        e.preventDefault()
        createOrUpdate(true).toastOnError(ToastMode.Infinit)
      }),
    ),
    Button(ButtonStyle.LightDefault, "Cancel", onClick.foreach(_ => cancelEdit())),
  )

  val mobileActions = Seq(
    Button(
      ButtonStyle.LightPrimary,
      cls := "min-h-8",
      Signal.dynamic {
        if (
          existingValue.flatMap(existingValue =>
            editingValue.value.map((_, a) => a.value == existingValue.signal.value),
          ) == Some(false)
        ) span(cls := "inline-block", icons.Save(cls := "w-4 h-4"))
        else span(cls := "inline-block", icons.Check(cls := "w-4 h-4"))
      },
      onClick.foreach(e => {
        e.preventDefault()
        createOrUpdate(false, true)
          .map(id => jsImplicits.routing.to(EditContractsPage(id), false, Map.empty, true))
          .toastOnError(ToastMode.Infinit)
      }),
    ),
    Button(
      ButtonStyle.LightPrimary,
      cls := "min-h-8",
      icons.Save(cls := "w-4 h-4"),
      onClick.foreach(e => {
        e.preventDefault()
        createOrUpdate().toastOnError(ToastMode.Infinit)
      }),
    ),
    Button(
      ButtonStyle.LightPrimary,
      cls := "min-h-8",
      icons.Save(cls := "w-4 h-4"),
      onClick.foreach(e => {
        e.preventDefault()
        createOrUpdate(true).toastOnError(ToastMode.Infinit)
      }),
    ),
    Button(
      ButtonStyle.LightDefault,
      cls := "min-h-8",
      icons.Close(cls := "w-4 h-4"),
      onClick.foreach(_ => cancelEdit()),
    ),
  )

  def render: VNode = {
    val ctrlSListener: js.Function1[KeyboardEvent, Unit] = (e: KeyboardEvent) => {
      if (e.keyCode == 83 && e.ctrlKey) {
        e.preventDefault()
        createOrUpdate(false, true)
          .map(id => jsImplicits.routing.to(EditContractsPage(id), false, Map.empty, true))
          .toastOnError(ToastMode.Infinit)
      }
    }
    navigationHeader(
      onDomMount.foreach(_ => document.addEventListener("keydown", ctrlSListener)),
      onDomUnmount.foreach(_ => document.removeEventListener("keydown", ctrlSListener)),
      div(
        div(
          cls := "p-1",
          h1(
            cls := "text-3xl mt-4 text-center",
            Signal.dynamic {
              editingValue.value.map((_, value) =>
                if (value.value.isDraft.get.getOrElse(true)) "Edit Contract Draft" else "Edit Contract",
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
                  cls := "flex flex-col md:flex-row md:space-x-4",
                  div(
                    cls := "basis-1/2",
                    label(cls := "font-bold", "Hiwi:"),
                    ContractPageAttributes().contractAssociatedHiwi.renderEdit("", editingValue),
                  ),
                  div(
                    cls := "basis-1/2",
                    label(cls := "font-bold", "Supervisor:"),
                    ContractPageAttributes().contractAssociatedSupervisor.renderEdit("", editingValue),
                  ),
                ),
                div(
                  cls := "flex flex-col md:flex-row md:space-x-4",
                  div(
                    cls := "basis-2/5",
                    label(cls := "font-bold", "Start date:"),
                    ContractPageAttributes().contractStartDate.renderEdit("", editingValue),
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
                    ContractPageAttributes().contractEndDate.renderEdit("", editingValue),
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
                  cls := "flex flex-col md:flex-row md:space-x-4",
                  div( // TODO calculation of monthly base salary and total hours
                    cls := "basis-1/2",
                    p(
                      cls := "p-4 bg-blue-100 dark:bg-blue-200 dark:text-blue-600",
                      "Monthly base salary: ",
                      Signal.dynamic {
                        editingValue.value match {
                          case None => span()
                          case Some(c) => {
                            val contract = c(1).value
                            val limit =
                              ContractPageAttributes()
                                .getLimit(contractId, contract, contract.contractStartDate.get.getOrElse(0L))
                                .value
                            val hourlyWage =
                              ContractPageAttributes()
                                .getMoneyPerHour(contractId, contract, contract.contractStartDate.get.getOrElse(0L))
                                .value
                            if (hourlyWage != 0 && limit != 0) {
                              val maxHours = (limit / hourlyWage).setScale(0, RoundingMode.FLOOR)
                              span(
                                toMoneyString(contract.contractHoursPerMonth.get.getOrElse(0) * hourlyWage),
                                span(
                                  cls := "text-gray-500 text-sm",
                                  s" (calculating with a salary of ${toMoneyString(hourlyWage)}/h that was set when the contract has started) Minijob Limit: ${toMoneyString(limit)} Maximum hours below limit: ${maxHours}",
                                ),
                              )
                            } else {
                              span(
                              )
                            }
                          }
                        }
                      },
                    ),
                    br,
                    p(
                      cls := "p-4 bg-blue-100 dark:bg-blue-200 dark:text-blue-600",
                      "Total Hours: ",
                      Signal.dynamic {
                        editingValue.value match {
                          case None => span()
                          case Some(c) => {
                            val contract = c(1).value
                            val month = dateDiffMonth(
                              contract.contractStartDate.get.getOrElse(0L),
                              contract.contractEndDate.get.getOrElse(0L),
                            )
                            span(
                              (contract.contractHoursPerMonth.get.getOrElse(0) * month),
                              span(
                                cls := "text-gray-500 text-sm",
                                s" (calculating with ${month} month)",
                              ),
                            )
                          }
                        }
                      },
                    ),
                  ),
                  div(
                    cls := "basis-1/2",
                    label(cls := "font-bold", "Payment Level:"),
                    ContractPageAttributes().contractAssociatedPaymentLevel.renderEdit("", editingValue),
                    label(cls := "font-bold", "Hours per month:"),
                    ContractPageAttributes().contractHoursPerMonth.renderEdit("", editingValue),
                    Signal.dynamic {
                      editingValue.value.map((_, contractSignal) => {
                        val contract = contractSignal.value
                        val project = contract.contractAssociatedProject.get.flatMap(id =>
                          jsImplicits.repositories.projects.all.value
                            .find(project => project.id == id),
                        )
                        val limit =
                          ContractPageAttributes()
                            .getLimit(contractId, contract, contract.contractStartDate.get.getOrElse(0L))
                            .value
                        val hourlyWage = ContractPageAttributes()
                          .getMoneyPerHour(contractId, contract, contract.contractStartDate.get.getOrElse(0L))
                          .value
                        var maxHoursForTax =
                          if (hourlyWage != 0) (limit / hourlyWage).setScale(0, RoundingMode.FLOOR)
                          else { BigDecimal(0) }
                        val month = dateDiffMonth(
                          contract.contractStartDate.get.getOrElse(0L),
                          contract.contractEndDate.get.getOrElse(0L),
                        )

                        project.map(project => {
                          val totalHoursWithoutThisContract = ProjectAttributes()
                            .countContractHours(
                              contract.contractAssociatedProject.get.getOrElse(""),
                              project.signal.value,
                              (id, contract) => !contract.isDraft.get.getOrElse(true) && id != contractId,
                            )
                            .value +
                            ProjectAttributes()
                              .countContractHours(
                                contract.contractAssociatedProject.get.getOrElse(""),
                                project.signal.value,
                                (id, contract) => contract.isDraft.get.getOrElse(true) && id != contractId,
                              )
                              .value

                          val maxHoursForProject =
                            (project.signal.value.maxHours.get.getOrElse(0) - totalHoursWithoutThisContract) / month

                          contract.contractHoursPerMonth.get.getOrElse(0) match {
                            case x if x > maxHoursForTax =>
                              p(
                                cls := "bg-yellow-100 text-yellow-600 flex flex-row",
                                icons.WarningTriangle(cls := "w-6 h-6"),
                                s"The monthly wage is above the minijob limit which is ${limit}. You might want to reduce the hours to ${maxHoursForTax} hours.",
                              )
                            case x
                                if x * month + totalHoursWithoutThisContract > project.signal.value.maxHours.get
                                  .getOrElse(0) =>
                              p(
                                cls := "bg-yellow-100 text-yellow-600 flex flex-row",
                                icons.WarningTriangle(cls := "w-6 h-6"),
                                s"Together with the other contracts and contract drafts assigned to this project the maximum amount of hours is exceeded! You might want to reduce the monthly hours to ${if (maxHoursForProject < 0) 0 else maxHoursForProject}.",
                              )
                            case _ => p()
                          }
                        })
                      })
                    },
                  ),
                ),
              ),
            ),

            // Select Project Field
            editStep(
              "1b",
              "Select project",
              div(
                cls := "flex flex-col md:flex-row p-4 md:space-x-4",
                div(
                  cls := "basis-1/2",
                  label(cls := "font-bold", "Project:"),
                  ContractPageAttributes().contractAssociatedProject.renderEdit("", editingValue),
                ),
                div(
                  cls := "basis-[12.5%] flex flex-row md:flex-col justify-between",
                  label(
                    cls := "font-bold",
                    Signal.dynamic {
                      editingValue.value.map((_, value) =>
                        if (value.value.isDraft.get.getOrElse(true)) "Contracts:" else "Other Contracts:",
                      )
                    },
                  ),
                  div(
                    Signal.dynamic {
                      editingValue.value.flatMap((_, value) =>
                        value.value.contractAssociatedProject.get.flatMap(id =>
                          jsImplicits.repositories.projects.all.value
                            .find(project => project.id == id)
                            .map(project =>
                              (ProjectAttributes()
                                .countContractHours(
                                  id,
                                  project.signal.value,
                                  (id, contract) => !contract.isDraft.get.getOrElse(true) && id != contractId,
                                )
                                .value)
                                .toString() + " h",
                            ),
                        ),
                      )
                    },
                  ),
                ),
                div(
                  cls := "basis-[12.5%] flex flex-row md:flex-col justify-between",
                  label(
                    cls := "font-bold",
                    Signal.dynamic {
                      editingValue.value.map((_, value) =>
                        if (value.value.isDraft.get.getOrElse(true)) "Other Drafts:" else "Drafts:",
                      )
                    },
                  ),
                  div(
                    Signal.dynamic {
                      editingValue.value.flatMap((_, value) =>
                        value.value.contractAssociatedProject.get.flatMap(id =>
                          jsImplicits.repositories.projects.all.value
                            .find(project => project.id == id)
                            .map(project =>
                              (ProjectAttributes()
                                .countContractHours(
                                  id,
                                  project.signal.value,
                                  (id, contract) => contract.isDraft.get.getOrElse(true) && id != contractId,
                                )
                                .value)
                                .toString() + " h",
                            ),
                        ),
                      )
                    },
                  ),
                ),
                div(
                  cls := "basis-[12.5%] flex flex-row md:flex-col justify-between",
                  label(
                    cls := "font-bold",
                    Signal.dynamic {
                      editingValue.value.map((_, value) =>
                        if (value.value.isDraft.get.getOrElse(true)) "This Draft:" else "This Contract:",
                      )
                    },
                  ),
                  div(
                    Signal.dynamic {
                      editingValue.value.map((_, value) =>
                        s"${ContractPageAttributes().getTotalHours(contractId, value.value)} h",
                      )
                    },
                  ),
                ),
                div(
                  cls := "basis-[12.5%] flex flex-row md:flex-col justify-between",
                  label(cls := "font-bold", "Max. hours"),
                  div(Signal.dynamic {
                    editingValue.value.flatMap((_, value) =>
                      value.value.contractAssociatedProject.get.flatMap(id =>
                        jsImplicits.repositories.projects.all.value
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
                ContractPageAttributes().contractAssociatedType.renderEdit("", editingValue),
              ),
            ),
            // Contract Requirements
            editStep(
              "3a",
              "Contract requirements - reminder mail",
              div(
                cls := "p-4",
                Button(
                  ButtonStyle.Primary,
                  "Send Reminder",
                  onClick.foreach(_ => {
                    editingValue.now.map((contract, _) => {
                      val supervisorOption = jsImplicits.repositories.supervisors.all.now.find(p =>
                        p.id == contract.contractAssociatedSupervisor.get.getOrElse(""),
                      )
                      val hiwiOption =
                        jsImplicits.repositories.hiwis.all.now.find(p =>
                          p.id == contract.contractAssociatedHiwi.get.getOrElse(""),
                        )

                      if (hiwiOption.nonEmpty && supervisorOption.nonEmpty) {
                        val hiwi = hiwiOption.get.signal.now
                        val supervisor = supervisorOption.get.signal.now

                        jsImplicits.mailing.sendMail(
                          hiwi.eMail.get.getOrElse(""),
                          supervisor.eMail.get.getOrElse(""),
                          h1("A very cool mail ", hiwi.firstName.get.getOrElse(""), cls := "color:red"),
                        )
                      }

                    })
                  }),
                ),
              ),
            ),
            // Check requirements
            editStep(
              "3",
              "Check requirements",
              div(
                cls := "p-4",
                "Check all forms the hiwi has filled out and handed back.",
                ContractPageAttributes().requiredDocuments.renderEdit("", editingValue),
                i(
                  "Documents written in italic have been checked in an older contract type and will be removed from this list once unchecked.",
                ),
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

                    editingValue.now.map((_, contractOption) => {
                      val contract = contractOption.now
                      val hiwiOption =
                        jsImplicits.repositories.hiwis.all.now
                          .find(_.id == contract.contractAssociatedHiwi.get.getOrElse(""))
                      val paymentLevelOption = jsImplicits.repositories.paymentLevels.all.now
                        .find(_.id == contract.contractAssociatedPaymentLevel.get.getOrElse(""))

                      if (hiwiOption.nonEmpty && paymentLevelOption.nonEmpty) {
                        val hiwi = hiwiOption.get.signal.now
                        val paymentLevel = paymentLevelOption.get.signal.now
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
                              document.getElementById("loadPDF").classList.remove("loading")
                            })
                            .toastOnError()
                        }.toFuture
                          .toastOnError()
                      } else {
                        jsImplicits.toaster.make(
                          "The PDF could not be created because not all required fields are filled in!",
                          ToastMode.Long,
                          ToastType.Error,
                        )
                        document.getElementById("loadLetter").classList.remove("loading")
                      }
                    })
                  }),
                ),
                label(
                  ContractPageAttributes().signed.renderEdit("", editingValue),
                  " The contract has been signed",
                  cls := "mt-2 flex gap-2",
                ),
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

                    editingValue.now.map((_, contractOption) => {
                      val contract = contractOption.now
                      val hiwiOption =
                        jsImplicits.repositories.hiwis.all.now
                          .find(_.id == contract.contractAssociatedHiwi.get.getOrElse(""))
                      val paymentLevelOption = jsImplicits.repositories.paymentLevels.all.now
                        .find(_.id == contract.contractAssociatedPaymentLevel.get.getOrElse(""))
                      val projectOption =
                        jsImplicits.repositories.projects.all.now
                          .find(_.id == contract.contractAssociatedProject.get.getOrElse(""))

                      if (hiwiOption.nonEmpty && paymentLevelOption.nonEmpty && projectOption.nonEmpty) {
                        val hiwi = hiwiOption.get.signal.now
                        val paymentLevel = paymentLevelOption.get.signal.now
                        val project = projectOption.get.signal.now
                        val moneyPerHour =
                          toMoneyString(
                            ContractPageAttributes()
                              .getMoneyPerHour(contractId, contract, contract.contractStartDate.get.getOrElse(0L))
                              .now,
                          )
                        val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
                        val totalHours = dateDiffMonth(
                          contract.contractStartDate.get.getOrElse(0L),
                          contract.contractEndDate.get.getOrElse(0L),
                        ) * hoursPerMonth
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
                                PDFTextField(
                                  "€StdRow1",
                                  moneyPerHour,
                                ),
                                PDFTextField("Stunden gesamtRow1", totalHours.toString),
                                PDFTextField("Stunden gesamtSumme", totalHours.toString),
                                PDFTextField(
                                  "Std MonatRow1",
                                  hoursPerMonth.toString,
                                ),
                                PDFTextField(
                                  "Account",
                                  project.accountName.get.flatten.getOrElse(""),
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
                      } else {
                        jsImplicits.toaster.make(
                          "The PDF could not be created because not all required fields are filled in!",
                          ToastMode.Long,
                          ToastType.Error,
                        )
                        document.getElementById("loadLetter").classList.remove("loading")
                      }
                    })
                  }),
                ),
                label(
                  ContractPageAttributes().submitted.renderEdit("", editingValue),
                  " The letter has been submitted",
                  cls := "mt-2 flex gap-2",
                ),
              ),
            ),
            div(
              idAttr := "static_buttons",
              cls := "md:pl-8 md:space-x-4 flex flex-col md:flex-row gap-2",
              actions,
            ),
          ),
        ),
        div(
          idAttr := "sticky_buttons",
          onDomMount.foreach(_ => stickyButton("#static_buttons", "#sticky_buttons", "hidden")),
          cls := "left-4 md:space-x-4 fixed bottom-4 p-3 bg-slate-50/75 dark:bg-gray-500/75 dark:border-gray-500 shadow-lg rounded-xl border border-slate-200 hidden",
          div(cls := "flex-row gap-2 hidden md:flex", actions),
          div(cls := "flex flex-row gap-2 md:hidden", mobileActions),
        ),
      ),
    )
  }

}
