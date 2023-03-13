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
import webapp.services.ReminderMail
import webapp.services.DekanatMail
import webapp.services.ContractEmail
import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import scala.util.Failure
import webapp.components.icons.Edit

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

case class ExtendContractPage(contractId: String)(using
    jsImplicits: JSImplicits,
) extends Page {

  private val existingValue = jsImplicits.repositories.contracts.all.map(_.find(c => c.id == contractId))
  def render: VNode = {
    div(
      existingValue
        .map(currentContract => {
          val result: VMod = currentContract match {
            case Some(currentContract) =>
              InnerExtendContractsPage(Some(currentContract), contractId).render()
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

abstract class Step(
    number: String,
    title: String,
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using jsImplicits: JSImplicits) {

  protected def updateHoursPerMonth(hours: Int) = {
    editingValue.now.map((_, a) => a.transform(c => c.copy(contractHoursPerMonth = c.contractHoursPerMonth.set(hours))))
  }

  protected def editStep(children: VMod*): VNode = {
    div(
      cls := "relative",
      Signal {
        if (
          !disabledDescription
            .isBlank() && disabled.value.map((pred, _) => pred).fold[Boolean](false)((a, b) => a || b)
        ) {
          var reasons: Seq[String] = Seq.empty
          disabled.value.foreach((pred, reason) => if (pred) reasons = reasons :+ reason)

          Some(
            div(
              icons.Info(cls := "w-6 h-6 shrink-0	"),
              cls := "max-w-[400px] absolute top-1/2 -translate-y-1/2 left-1/2 -translate-x-1/2 max-x-[80%] rounded-lg bg-white p-2 z-[100] text-sm flex items-center flex-row gap-2 shadow-sm",
              div(
                if (reasons.length >= 2)
                  s"To $disabledDescription you need ${reasons.take(reasons.length - 1).mkString(", ")} and you need ${reasons(reasons.length - 1)}."
                else if (reasons.length == 1) s"To $disabledDescription you need ${reasons(0)}."
                else "",
              ),
            ),
          )
        } else None
      },
      div(
        cls := "border rounded-lg m-4 border-purple-200 dark:border-gray-500 dark:text-gray-200 overflow-hidden",
        cls <-- Signal {
          if (disabled.value.map((pred, _) => pred).fold[Boolean](false)((a, b) => a || b))
            "blur-[3px] pointer-events-none select-none	"
          else ""
        },
        div(
          cls := "bg-purple-200 p-4 dark:bg-gray-700 dark:text-gray-200 text-purple-600",
          div(cls := "flex justify-between", div(title) /*, div(cls := "font-bold text-md", s"$number")*/ ),
        ),
        children,
      ),
    )
  }

  protected def letterPDF: Future[Try[ArrayBuffer[Short]]] = {
    val promise: Promise[Try[ArrayBuffer[Short]]] = Promise()

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
              .getMoneyPerHour(existingId, contract, contract.contractStartDate.get.getOrElse(0L))
              .now,
          )
        val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
        val totalHours = dateDiffMonth(
          contract.contractStartDate.get.getOrElse(0L),
          contract.contractEndDate.get.getOrElse(0L),
        ) * hoursPerMonth
        PDF
          .fill(
            "/letter_editable.pdf",
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
          .andThen(a => promise.success(a))
          .toastOnError()
      } else {
        promise.failure(new Exception("The PDF could not be created because not all required fields are filled in!"))

      }
    })

    promise.future
  }

  protected def contractPDF: Future[Try[ArrayBuffer[Short]]] = {
    val promise: Promise[Try[ArrayBuffer[Short]]] = Promise()
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
        PDF
          .fill(
            "/contract_unlocked.pdf",
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
          .andThen(a => promise.success(a))
          .toastOnError()
      } else {
        promise.failure(new Exception("The PDF could not be created because not all required fields are filled in!"))
      }
    })

    promise.future
  }

  def render: VNode
}

class BasicInformation(
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
    extend: Boolean = false,
)(using
    jsImplicits: JSImplicits,
) extends Step("1", "Basic Information", existingId, existingValue, editingValue, disabled, disabledDescription) {
  def render: VNode = {
    this.editStep(
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
                      cls := "bg-yellow-100 text-yellow-600 flex flex-row p-4 rounded-md gap-2 mt-2 text-sm",
                      icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
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
                      cls := "bg-yellow-100 text-yellow-600 flex flex-row p-4 rounded-md gap-2 mt-2 text-sm",
                      icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
                      "End date is in the past or before start date",
                    ),
                  )
                } else None
              }),
            ),
          ),
        ),
        Signal.dynamic {
          val overlappingContract = editingValue.value
            .map((_, contractSignal) => {
              val start = contractSignal.value.contractStartDate.get.getOrElse(0L)
              val end = contractSignal.value.contractEndDate.get.getOrElse(0L)
              val hiwi = contractSignal.value.contractAssociatedHiwi.get.getOrElse("")
              val overlappingContracts = jsImplicits.repositories.contracts.existing.value.filter(c => {
                val contract = c.signal.value
                contract.isDraft.get.getOrElse(true) == false &&
                contract.contractAssociatedHiwi.get.nonEmpty &&
                contract.contractAssociatedHiwi.get.get == hiwi &&
                contract.contractStartDate.get.nonEmpty &&
                contract.contractEndDate.get.nonEmpty &&
                (
                  (
                    contract.contractStartDate.get.getOrElse(0L) <= start &&
                      contract.contractEndDate.get.getOrElse(0L) >= start
                  ) ||
                    (
                      (contract.contractStartDate.get.getOrElse(0L) <= end) &&
                        contract.contractEndDate.get.getOrElse(0L) >= end
                    ) ||
                    (
                      contract.contractStartDate.get.getOrElse(0L) >= start &&
                        contract.contractEndDate.get.getOrElse(0L) <= end
                    )
                )
              })

              overlappingContracts.size > 0
            })
            .getOrElse(false)

          if (overlappingContract) {
            Some(
              p(
                cls := "bg-yellow-100 text-yellow-600 flex flex-row p-4 rounded-md gap-2 mt-2 text-sm",
                icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
                "This hiwi aleady has a finalized contract that overlaps with your selected contract period.",
              ),
            )
          } else None
        },
        div(
          cls := "flex flex-col md:flex-row md:space-x-4",
          div(
            cls := "basis-1/2",
            p(
              cls := "",
              Signal.dynamic {
                editingValue.value.map((_, c) => {
                  val contract = c.value
                  val limit =
                    ContractPageAttributes()
                      .getLimit(existingId, contract, contract.contractStartDate.get.getOrElse(0L))
                      .value
                  val hourlyWage =
                    ContractPageAttributes()
                      .getMoneyPerHour(existingId, contract, contract.contractStartDate.get.getOrElse(0L))
                      .value

                  val month = dateDiffMonth(
                    contract.contractStartDate.get.getOrElse(0L),
                    contract.contractEndDate.get.getOrElse(0L),
                  )

                  if (hourlyWage != 0 && limit != 0) {
                    val maxHours = (limit / hourlyWage).setScale(0, RoundingMode.FLOOR)
                    div(
                      cls := "flex flex-col gap-2",
                      div(
                        cls := "flex flex-col gap-1",
                        span(
                          span(cls := "text-sm text-slate-600", "Base Salary: "),
                          span(
                            cls := "font-bold",
                            toMoneyString(contract.contractHoursPerMonth.get.getOrElse(0) * hourlyWage),
                          ),
                        ),
                        span(
                          cls := "text-slate-400 text-xs italic",
                          s"calculating with a salary of ${toMoneyString(hourlyWage)}/h that was set when the contract has started",
                        ),
                      ),
                      div(
                        cls := "flex flex-col gap-1",
                        span(
                          span(cls := "text-sm text-slate-600", "Minijob Limit: "),
                          span(cls := "font-bold", toMoneyString(limit)),
                        ),
                        span(
                          cls := "text-slate-400 text-xs italic",
                          s"the limit when the contract has started",
                        ),
                      ),
                      div(
                        cls := "flex flex-col gap-1",
                        span(
                          span(cls := "text-sm text-slate-600", "Maxmimum Hours below Limit: "),
                          span(cls := "font-bold", maxHours.toInt),
                        ),
                      ),
                      div(
                        cls := "flex flex-col gap-1",
                        span(
                          span(cls := "text-sm text-slate-600", "Total hours: "),
                          span(cls := "font-bold", contract.contractHoursPerMonth.get.getOrElse(0) * month),
                        ),
                        span(
                          cls := "text-slate-400 text-xs",
                          s"calculating with ${month} month",
                        ),
                      ),
                    )
                  } else {
                    span()
                  }
                })
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
                    .getLimit(existingId, contract, contract.contractStartDate.get.getOrElse(0L))
                    .value
                val hourlyWage = ContractPageAttributes()
                  .getMoneyPerHour(existingId, contract, contract.contractStartDate.get.getOrElse(0L))
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
                      (id, contract) => !contract.isDraft.get.getOrElse(true) && id != existingId,
                    )
                    .value +
                    ProjectAttributes()
                      .countContractHours(
                        contract.contractAssociatedProject.get.getOrElse(""),
                        project.signal.value,
                        (id, contract) => contract.isDraft.get.getOrElse(true) && id != existingId,
                      )
                      .value

                  val maxHoursForProject =
                    (project.signal.value.maxHours.get.getOrElse(0) - totalHoursWithoutThisContract) / month

                  contract.contractHoursPerMonth.get.getOrElse(0) match {
                    case x if x > maxHoursForTax =>
                      p(
                        cls := "bg-yellow-100 text-yellow-600 flex flex-row p-4 rounded-md gap-2 mt-2 text-sm",
                        icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
                        span(
                          s"The monthly wage is above the minijob limit which is ${toMoneyString(limit)}. You might want to ",
                          span(
                            cls := "underline cursor-pointer",
                            onClick.foreach(_ => {
                              this.updateHoursPerMonth(maxHoursForTax.toInt)

                            }),
                            s"reduce the hours to ${maxHoursForTax} hours.",
                          ),
                        ),
                      )
                    case x
                        if x * month + totalHoursWithoutThisContract > project.signal.value.maxHours.get
                          .getOrElse(0) && !extend =>
                      p(
                        cls := "bg-yellow-100 text-yellow-600 flex flex-row p-2 rounded-lg gap-2 mt-2 text-sm",
                        icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
                        span(
                          s"Together with the other contracts and contract drafts assigned to this project the maximum amount of hours is exceeded! ",
                          span(
                            cls := "underline cursor-pointer",
                            onClick.foreach(_ => {
                              this.updateHoursPerMonth(
                                if (maxHoursForProject < 0) 0
                                else maxHoursForProject,
                              )
                            }),
                            s"You might want to reduce the monthly hours to ${if (maxHoursForProject < 0) 0
                              else maxHoursForProject}.",
                          ),
                        ),
                      )
                    case _ => p()
                  }
                })
              })
            },
          ),
        ),
      ),
    )
  }
}

class SelectProject(
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("1b", "Select Project", existingId, existingValue, editingValue, disabled, disabledDescription) {
  def render: VNode = {
    this.editStep(
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
                          (id, contract) => !contract.isDraft.get.getOrElse(true) && id != existingId,
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
                          (id, contract) => contract.isDraft.get.getOrElse(true) && id != existingId,
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
                s"${ContractPageAttributes().getTotalHours(existingId, value.value)} h",
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
    )
  }
}

class ContractType(
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("2", "Contract Type", existingId, existingValue, editingValue, disabled, disabledDescription) {
  def render: VNode = {
    this.editStep(
      div(
        cls := "p-4",
        label(cls := "font-bold", "Contract type:"),
        ContractPageAttributes().contractAssociatedType.renderEdit("", editingValue),
      ),
    )
  }
}

class ContractRequirements(
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("3", "Contract requirements", existingId, existingValue, editingValue, disabled, disabledDescription) {
  def render: VNode = {
    this.editStep(
      div(
        cls := "p-4",
        "Check all forms the hiwi has filled out and handed back.",
        ContractPageAttributes().requiredDocuments.renderEdit("", editingValue),
        i(
          cls := "text-slate-400 text-xs",
          "Documents written in italic have been checked in an older contract type and will be removed from this list once unchecked.",
        ),
      ),
    )
  }
}

class ContractRequirementsMail(
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    save: () => Unit,
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step(
      "3a",
      "Contract requirements - reminder mail",
      existingId,
      existingValue,
      editingValue,
      disabled,
      disabledDescription,
    ) {
  def render: VNode = {
    this.editStep(
      div(
        cls := "p-4 flex flex-col gap-2",
        Button(
          ButtonStyle.LightPrimary,
          "Send Reminder",
          idAttr := "sendReminder",
          cls := "w-fit",
          onClick.foreach(e => {
            e.preventDefault()
            document.querySelector("#sendReminder").classList.add("loading")

            editingValue.now.map((contract, _) => {
              val supervisorOption = jsImplicits.repositories.supervisors.all.now.find(p =>
                p.id == contract.contractAssociatedSupervisor.get.getOrElse(""),
              )
              val hiwiOption =
                jsImplicits.repositories.hiwis.all.now.find(p =>
                  p.id == contract.contractAssociatedHiwi.get.getOrElse(""),
                )

              val contractTypeOption = jsImplicits.repositories.contractSchemas.all.now.find(p =>
                p.id == contract.contractType.get.getOrElse(""),
              )

              val documents = jsImplicits.repositories.requiredDocuments.all.now

              if (hiwiOption.nonEmpty && supervisorOption.nonEmpty && contractTypeOption.nonEmpty) {
                val hiwi = hiwiOption.get.signal.now
                val supervisor = supervisorOption.get.signal.now
                val neededDocuments = contractTypeOption.get.signal.now.files.get
                  .getOrElse(Seq.empty)
                  .filter(p => !contract.requiredDocuments.get.getOrElse(Seq.empty).contains(p))
                  .map(documentId =>
                    documents
                      .find(p => p.id == documentId)
                      .map(document => document.signal.now.name.get.getOrElse(""))
                      .getOrElse(""),
                  )

                jsImplicits.mailing
                  .sendMail(
                    hiwi.eMail.get.getOrElse(""),
                    supervisor.eMail.get.getOrElse(""),
                    supervisor.name.get.getOrElse(""),
                    ReminderMail(
                      hiwi,
                      supervisor,
                      (js.Date.now + 12096e5).toLong, // magic Number is 14 days in ms
                      neededDocuments,
                    ),
                    Seq(supervisor.eMail.get.getOrElse("")),
                  )
                  .andThen(ans => {
                    document.querySelector("#sendReminder").classList.remove("loading")
                    if (ans.get.rejected.length > 0) {
                      jsImplicits.toaster.make(s"Could not deliver mail to ${ans.get.rejected.mkString(" and ")}.")
                    }
                    if (ans.get.accepted.length > 0) {
                      Signal {
                        editingValue.value
                          .map((_, contract) => {
                            contract
                              .transform(contract => contract.copy(reminderSentDate = Attribute(js.Date.now.toLong)))
                            save()
                          })
                      }
                      jsImplicits.toaster.make(s"Sent mail to ${ans.get.accepted.mkString(" and ")}.")
                    }
                  })
                  .toastOnError()
              }

            })
          }),
        ),
        div(
          cls := "text-xs text-slate-400 italic",
          "Last sent: ",
          span(
            cls := "bg-purple-200 p-1 rounded-md text-purple-600",
            Signal.dynamic {
              val date = editingValue.value
                .flatMap((_, contract) => contract.value.reminderSentDate.get)
                .getOrElse(0L)

              if (date > 0L) toGermanDate(date) else "Never"
            },
          ),
        ),
      ),
    )
  }
}

class CreateContract(
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    save: () => Unit,
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("4", "Create Documents", existingId, existingValue, editingValue, disabled, disabledDescription) {
  def render: VNode = {
    this.editStep(
      div(
        cls := "p-4 flex flex-col",
        div(
          cls := "flex gap-2",
          Button(
            ButtonStyle.LightPrimary,
            "Create Contract PDF",
            idAttr := "loadPDF",
            onClick.foreach(e => {
              e.preventDefault()
              document.getElementById("loadPDF").classList.add("loading")
              this.contractPDF
                .andThen(v => {
                  document.getElementById("loadPDF").classList.remove("loading")
                  if (v.isSuccess) {
                    val buffer = v.get.get
                    PDF.download("contract.pdf", buffer)
                  }
                })
                .toastOnError()
            }),
          ),
          div(
            cls := "flex flex-col gap-2",
            Button(
              ButtonStyle.LightDefault,
              "Send Contract PDF",
              idAttr := "sendContract",
              dsl.disabled <-- jsImplicits.discovery.online.map(!_),
              onClick.foreach(e => {
                e.preventDefault()
                document.querySelector("#sendContract").classList.add("loading")

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

                    this.contractPDF
                      .andThen(cotract => {
                        jsImplicits.mailing
                          .sendMail(
                            hiwi.eMail.get.getOrElse(""),
                            supervisor.eMail.get.getOrElse(""),
                            supervisor.name.get.getOrElse(""),
                            ContractEmail(
                              hiwi,
                              supervisor,
                              (js.Date.now + 12096e5).toLong, // magic Number is 14 days in ms
                              cotract.get.get,
                            ),
                            Seq(supervisor.eMail.get.getOrElse("")),
                          )
                          .andThen(ans => {
                            document.querySelector("#sendContract").classList.remove("loading")
                            if (ans.get.rejected.length > 0) {
                              jsImplicits.toaster
                                .make(s"Could not deliver mail to ${ans.get.rejected.mkString(" and ")}.")
                            }
                            if (ans.get.accepted.length > 0) {
                              Signal {
                                editingValue.value
                                  .map((_, contract) => {
                                    contract
                                      .transform(contract =>
                                        contract.copy(contractSentDate = Attribute(js.Date.now.toLong)),
                                      )
                                  })
                                save()
                              }
                              jsImplicits.toaster.make(s"Sent mail to ${ans.get.accepted.mkString(" and ")}.")
                            }
                          })
                          .toastOnError()
                      })
                      .toastOnError()
                  }
                })
              }),
            ),
            div(
              cls := "text-xs text-slate-400 italic",
              "Last sent: ",
              span(
                cls := "bg-purple-200 p-1 rounded-md text-purple-600",
                Signal.dynamic {
                  val date = editingValue.value
                    .flatMap((_, contract) => contract.value.contractSentDate.get)
                    .getOrElse(0L)

                  if (date > 0L) toGermanDate(date) else "Never"
                },
              ),
            ),
          ),
        ),
        label(
          ContractPageAttributes().signed.renderEdit("", editingValue),
          " The contract has been signed",
          cls := "mt-2 flex gap-2",
        ),
      ),
    )
  }
}

class CreateLetter(
    existingId: String,
    existingValue: Option[Synced[Contract]],
    editingValue: Var[Option[(Contract, Var[Contract])]],
    save: () => Unit,
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("5", "Letter to Dekanat", existingId, existingValue, editingValue, disabled, disabledDescription) {
  def render: VNode = {
    this.editStep(
      div(
        cls := "p-4 flex flex-col",
        div(
          cls := "flex flex-row gap-2",
          Button(
            ButtonStyle.LightPrimary,
            "Create Letter",
            idAttr := "loadLetter",
            onClick.foreach(e => {
              e.preventDefault()
              document.getElementById("loadLetter").classList.add("loading")
              this.letterPDF
                .andThen(v => {
                  document.getElementById("loadPDF").classList.remove("loading")
                  if (v.isSuccess) {
                    val buffer = v.get.get
                    PDF.download("contract.pdf", buffer)
                  }
                })
                .toastOnError()
            }),
          ),
          div(
            cls := "flex flex-col gap-2",
            Button(
              ButtonStyle.LightDefault,
              "Send Letter",
              idAttr := "sendLetter",
              dsl.disabled <-- jsImplicits.discovery.online.map(!_),
              onClick.foreach(e => {
                e.preventDefault()
                document.querySelector("#sendLetter").classList.add("loading")
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

                    this.letterPDF
                      .andThen(letter => {
                        jsImplicits.mailing
                          .sendMail(
                            Globals.VITE_DEKANAT_MAIL,
                            supervisor.eMail.get.getOrElse(""),
                            supervisor.name.get.getOrElse(""),
                            DekanatMail(
                              hiwi,
                              supervisor,
                              letter.get.get,
                            ),
                            Seq(supervisor.eMail.get.getOrElse("")),
                          )
                          .andThen(ans => {
                            document.querySelector("#sendLetter").classList.remove("loading")
                            if (ans.get.rejected.length > 0) {
                              jsImplicits.toaster
                                .make(s"Could not deliver mail to ${ans.get.rejected.mkString(" and ")}.")
                            }
                            if (ans.get.accepted.length > 0) {
                              Signal {
                                editingValue.value
                                  .map((_, contract) => {
                                    contract
                                      .transform(contract =>
                                        contract.copy(letterSentDate = Attribute(js.Date.now.toLong)),
                                      )
                                  })
                                save()
                              }
                              jsImplicits.toaster.make(s"Sent mail to ${ans.get.accepted.mkString(" and ")}.")
                            }
                          })
                          .toastOnError()
                      })
                      .toastOnError()
                  }
                })
              }),
            ),
            div(
              cls := "text-xs text-slate-400 italic",
              "Last sent: ",
              span(
                cls := "bg-purple-200 p-1 rounded-md text-purple-600",
                Signal.dynamic {
                  val date = editingValue.value
                    .flatMap((_, contract) => contract.value.letterSentDate.get)
                    .getOrElse(0L)

                  if (date > 0L) toGermanDate(date) else "Never"
                },
              ),
            ),
          ),
        ),
        label(
          ContractPageAttributes().submitted.renderEdit("", editingValue),
          " The letter has been submitted",
          cls := "mt-2 flex gap-2",
        ),
      ),
    )
  }
}

class InnerExtendContractsPage(override val existingValue: Option[Synced[Contract]], override val contractId: String)(
    using jsImplicits: JSImplicits,
) extends InnerEditContractsPage(existingValue, contractId) {

  protected def createDraft(): Future[String] = {
    jsImplicits.indexeddb.requestPersistentStorage

    val editingNow = editingValue.now.get._2.now
    val draftContract = Contract(
      Attribute.empty,
      editingNow.contractAssociatedHiwi,
      editingNow.contractAssociatedPaymentLevel,
      editingNow.contractAssociatedSupervisor,
      editingNow.contractStartDate,
      editingNow.contractEndDate,
      Attribute.empty,
      editingNow.contractHoursPerMonth,
      Attribute(true),
      Attribute.empty,
      Attribute(false),
      Attribute(false),
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute(true),
    )
    jsImplicits.repositories.contracts
      .create({
        draftContract
      })
      .map(entity => {
        editingValue.set(Some((Contract.empty.default, Var(Contract.empty.default))))
        jsImplicits.routing.to(EditContractsPage(entity.id))

        entity.id
      })
  }

  override def render: VNode = {
    navigationHeader(
      div(
        div(
          cls := "p-1",
          h1(
            cls := "text-3xl mt-4 text-center",
            "Extend Contract",
          ),
        ),
        div(
          cls := "relative shadow-md rounded-lg p-4 my-4 mx-[2.5%] inline-block overflow-y-visible w-[95%]",
          form(
            BasicInformation(contractId, existingValue, editingValue, Signal(Seq.empty), "", true).render,
            div(
              idAttr := "static_buttons",
              cls := "pl-4 flex flex-col md:flex-row gap-2",
              Button(
                ButtonStyle.LightPrimary,
                "Create Draft",
                onClick.foreach(e => {
                  e.preventDefault()
                  createDraft()
                }),
              ),
            ),
          ),
        ),
      ),
    )
  }
}

class InnerEditContractsPage(val existingValue: Option[Synced[Contract]], val contractId: String)(using
    jsImplicits: JSImplicits,
) {
  val startEditEntity: Option[Contract] = existingValue.map(_.signal.now)

  protected def isCompleted: Signal[Boolean] = {
    Signal.dynamic {
      editingValue.value
        .map((_, contractSignal) => {
          val contract = contractSignal.value
          val requiredDocuments = jsImplicits.repositories.contractSchemas.all.value
            .find(s => s.id == contract.contractType.get.getOrElse(""))
            .flatMap(t => t.signal.value.files.get)

          !contract.contractAssociatedHiwi.get.nonEmpty ||
          !contract.contractAssociatedSupervisor.get.nonEmpty ||
          !contract.contractStartDate.get.nonEmpty ||
          !contract.contractEndDate.get.nonEmpty ||
          !contract.contractAssociatedProject.get.nonEmpty ||
          !contract.contractHoursPerMonth.get.nonEmpty ||
          contract.contractHoursPerMonth.get.getOrElse(0) < 0 ||
          !contract.contractAssociatedPaymentLevel.get.nonEmpty ||
          contract.contractStartDate.get.getOrElse(0L) > contract.contractEndDate.get.getOrElse(0L) ||
          contract.contractEndDate.get.getOrElse(0L) < js.Date.now().toLong ||
          !contract.requiredDocuments.get.nonEmpty ||
          !contract.isSigned.get.getOrElse(false) ||
          !requiredDocuments
            .getOrElse(Seq.empty)
            .forall(id => contract.requiredDocuments.get.getOrElse(Seq.empty).contains(id))

        })
        .getOrElse(false)
    }
  }

  protected def createOrUpdate(
      finalize: Boolean = false,
      stayOnPage: Boolean = false,
      silent: Boolean = false,
  ): Future[String] = {
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
            if (!silent) {
              jsImplicits.toaster.make(
                "Contract saved!",
                ToastMode.Short,
                ToastType.Success,
              )
            }

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

  protected def cancelEdit(): Unit = {
    jsImplicits.routing.to(ContractsPage())
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
      disabled <-- isCompleted,
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

  protected val ctrlSListener: js.Function1[KeyboardEvent, Unit] = (e: KeyboardEvent) => {
    if (e.keyCode == 83 && e.ctrlKey) {
      e.preventDefault()
      createOrUpdate(false, true)
        .map(id => jsImplicits.routing.to(EditContractsPage(id), false, Map.empty, true))
        .toastOnError(ToastMode.Infinit)
    }
  }

  def render: VNode = {
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
            BasicInformation(contractId, existingValue, editingValue).render,
            SelectProject(contractId, existingValue, editingValue).render,
            ContractType(contractId, existingValue, editingValue).render,
            ContractRequirements(contractId, existingValue, editingValue).render,
            ContractRequirementsMail(
              contractId,
              existingValue,
              editingValue,
              () => createOrUpdate(false, true, false),
              Signal.dynamic {
                editingValue.value
                  .map((_, contractSignal) => {
                    val contract = contractSignal.value
                    Seq(
                      (!contract.contractAssociatedHiwi.get.nonEmpty -> "a hiwi"),
                      (!contract.contractAssociatedSupervisor.get.nonEmpty -> "a supervisor"),
                      (!contract.contractType.get.nonEmpty -> "a contract type"),
                      (!jsImplicits.discovery.online.value -> "to be connected to the discovery server"),
                    )
                  })
                  .getOrElse(Seq.empty)
              },
              "send a reminder email",
            ).render,
            CreateContract(
              contractId,
              existingValue,
              editingValue,
              () => createOrUpdate(false, true, true),
              Signal.dynamic {
                editingValue.value
                  .map((_, contractSignal) => {
                    val contract = contractSignal.value
                    Seq(
                      (!contract.contractAssociatedHiwi.get.nonEmpty -> "a hiwi"),
                      (!contract.contractEndDate.get.nonEmpty -> "an end date"),
                      (!contract.contractStartDate.get.nonEmpty -> "a start date"),
                      (!contract.contractAssociatedPaymentLevel.get.nonEmpty -> "a payment level"),
                      (!contract.contractHoursPerMonth.get.nonEmpty -> "to set hours per month"),
                    )
                  })
                  .getOrElse(Seq.empty)
              },
              "create and download a contract",
            ).render,
            CreateLetter(
              contractId,
              existingValue,
              editingValue,
              () => createOrUpdate(false, true, true),
              Signal.dynamic {
                editingValue.value
                  .map((_, contractSignal) => {
                    val contract = contractSignal.value
                    Seq(
                      (!contract.contractAssociatedHiwi.get.nonEmpty -> "a hiwi"),
                      (!contract.contractEndDate.get.nonEmpty -> "an end date"),
                      (!contract.contractStartDate.get.nonEmpty -> "a start date"),
                      (!contract.contractAssociatedPaymentLevel.get.nonEmpty -> "a payment level"),
                      (!contract.contractHoursPerMonth.get.nonEmpty -> "to set hours per month"),
                      (!contract.contractAssociatedProject.get.nonEmpty -> "a project"),
                      (!contract.isSigned.get.getOrElse(false) -> "a signed contract"),
                    )
                  })
                  .getOrElse(Seq.empty)
              },
              "create and download a letter",
            ).render,
            div(
              idAttr := "static_buttons",
              cls := "pl-4 flex flex-col md:flex-row gap-2",
              actions,
            ),
          ),
        ),
        div(
          idAttr := "sticky_buttons",
          onDomMount.foreach(_ => stickyButton("#static_buttons", "#sticky_buttons", "hidden")),
          cls := "left-4 md:space-x-4 fixed bottom-4 p-3 bg-slate-50/75 dark:bg-gray-500/75 dark:border-gray-500 shadow-lg rounded-xl border border-slate-200 hidden z-[200]",
          div(cls := "flex-row gap-2 hidden md:flex", actions),
          div(cls := "flex flex-row gap-2 md:hidden", mobileActions),
        ),
      ),
    )
  }

}
