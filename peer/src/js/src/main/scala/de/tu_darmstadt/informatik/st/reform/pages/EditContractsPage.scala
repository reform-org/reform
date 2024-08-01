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
package de.tu_darmstadt.informatik.st.reform.pages

import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.components.icons
import de.tu_darmstadt.informatik.st.reform.entity.*
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.*
import de.tu_darmstadt.informatik.st.reform.npm.PDF
import de.tu_darmstadt.informatik.st.reform.npm.PDFCheckboxField
import de.tu_darmstadt.informatik.st.reform.npm.PDFTextField
import de.tu_darmstadt.informatik.st.reform.repo.Synced
import de.tu_darmstadt.informatik.st.reform.services.*
import de.tu_darmstadt.informatik.st.reform.utils.Futures.*
import de.tu_darmstadt.informatik.st.reform.{*, given}
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*
import rescala.default
import rescala.default.*

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.*
import scala.math.BigDecimal.RoundingMode
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.util.*

import ContractsPage.*

// TODO FIXME implement this using the proper existingValue=none, editingValue=Some logic
case class NewContractPage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render: VMod = {
    jsImplicits.repositories.contracts
      .create(Contract.empty.default)
      .map(currentContract => {
        InnerEditContractsPage(currentContract, "").render
      })
  }
}

case class ExtendContractPage(contractId: String)(using
    jsImplicits: JSImplicits,
) extends Page {

  private val currentContract = jsImplicits.repositories.contracts.find(contractId)

  private val extendedContract = Signal.dynamic {
    currentContract.value.map { synced =>
      val contract = synced.signal.value
      synced.copy(
        value = Var(
          contract.copy(
            contractStartDate = contract.contractEndDate.update(dateAdd(_, days = 1)),
            contractEndDate = contract.contractEndDate.update(dateAdd(_, years = 1)),
          ),
        ),
      )
    }
  }

  def render: VMod = Signal.dynamic {
    extendedContract.value match {
      case Some(contract) => InnerExtendContractsPage(contract, contractId).render
      case None           => ErrorPage().error("Contract not found", "", "Show me all contracts", ContractsPage())
    }
  }
}

case class EditContractsPage(contractId: String)(using
    jsImplicits: JSImplicits,
) extends Page {

  private val existingValue = Signal { jsImplicits.repositories.contracts.all.value.find(c => c.id == contractId) }

  def render: VMod = {
    existingValue
      .map(currentContract => {
        val result: VMod = currentContract match {
          case Some(currentContract) =>
            InnerEditContractsPage(currentContract, contractId).render
          case None =>
            ErrorPage().error("Contract not found", "", "Show me all contract drafts", ContractDraftsPage())
        }
        result
      })
  }
}

abstract class Step(
    title: String,
    existingId: String,
    editingValue: Var[Contract],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using jsImplicits: JSImplicits) {

  protected def updateHoursPerMonth(hours: Int): Unit = {
    editingValue.transform(c => c.copy(contractHoursPerMonth = c.contractHoursPerMonth.set(hours)))
  }

  protected def editStep(children: VMod*): VNode = {
    div(
      cls := "relative",
      Signal {
        if (
          !disabledDescription.isBlank && disabled.value.map((pred, _) => pred).fold[Boolean](false)((a, b) => a || b)
        ) {
          var reasons: Seq[String] = Seq.empty
          disabled.value.foreach((pred, reason) => if (pred) reasons = reasons :+ reason)

          Some(
            div(
              icons.Info(cls := "w-6 h-6 shrink-0	"),
              cls := "w-full md:max-w-[400px] absolute top-1/2 -translate-y-1/2 left-1/2 -translate-x-1/2 max-x-[80%] rounded-lg bg-white p-2 z-[100] text-sm flex items-center flex-row gap-2 shadow-sm dark:text-gray-200 dark:bg-gray-700",
              div(
                if (reasons.length >= 2)
                  s"To $disabledDescription you need ${reasons.take(reasons.length - 1).mkString(", ")} and you need ${reasons.last}."
                else if (reasons.length == 1) s"To $disabledDescription you need ${reasons.head}."
                else "",
              ),
            ),
          )
        } else None
      },
      div(
        cls := "border rounded-lg m-4 border-purple-200 dark:border-gray-500 dark:text-gray-200 overflow-hidden",
        cls <-- Signal {
          if (disabled.value.exists((pred, _) => pred))
            "blur-[3px] pointer-events-none select-none"
          else
            ""
        },
        div(
          cls := "bg-purple-200 p-4 dark:bg-gray-700 dark:text-gray-200 text-purple-600",
          div(cls := "flex justify-between", div(title) /*, div(cls := "font-bold text-md", s"$number")*/ ),
        ),
        children,
      ),
    )
  }

  protected def fillLetterPDF(url: String): Future[ArrayBuffer[Short]] = {
    val contract = editingValue.now
    val hiwiOption =
      jsImplicits.repositories.hiwis
        .find(contract.contractAssociatedHiwi.getOrElse(""))
        .now
    val paymentLevelOption =
      jsImplicits.repositories.paymentLevels
        .find(contract.contractAssociatedPaymentLevel.getOrElse(""))
        .now
    val projectOption =
      jsImplicits.repositories.projects
        .find(contract.contractAssociatedProject.getOrElse(""))
        .now

    if (hiwiOption.isEmpty || paymentLevelOption.isEmpty || projectOption.isEmpty) {
      Future.failed(new Exception("The PDF could not be created because not all required fields are filled in!"))
    }

    val hiwi = hiwiOption.get.signal.now
    val project = projectOption.get.signal.now
    val moneyPerHour =
      toMoneyString(
        ContractPageAttributes()
          .getMoneyPerHour(existingId, contract, contract.contractStartDate.getOrElse(0L))
          .now,
      )
    val hoursPerMonth = contract.contractHoursPerMonth.getOrElse(0)
    val totalHours = dateDiffMonth(
      contract.contractStartDate.getOrElse(0L),
      contract.contractEndDate.getOrElse(0L),
    ) * hoursPerMonth

    PDF.fill(
      url,
      Seq(
        PDFTextField(
          "Name VornameRow1",
          s"${hiwi.lastName.getOrElse("")}, ${hiwi.firstName.getOrElse("")}",
        ),
        PDFTextField(
          "GebDatumRow1",
          s"${toGermanDate(hiwi.birthdate.getOrElse(0))}",
        ),
        PDFTextField(
          "Vertrags beginnRow1",
          toGermanDate(contract.contractStartDate.getOrElse(0)),
        ),
        PDFTextField(
          "Vertrags endeRow1",
          toGermanDate(contract.contractEndDate.getOrElse(0)),
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
          project.accountName.option.flatten.getOrElse(""),
        ),
        PDFTextField(
          "Datum",
          toGermanDate(js.Date.now().toLong),
        ),
      ),
    )
  }

  protected def fillContractPDF(url: String): Future[ArrayBuffer[Short]] = {
    val promise: Promise[ArrayBuffer[Short]] = Promise()
    val contract = editingValue.now

    val hiwiOption =
      jsImplicits.repositories.hiwis
        .find(contract.contractAssociatedHiwi.getOrElse(""))
        .now

    val paymentLevelOption =
      jsImplicits.repositories.paymentLevels
        .find(contract.contractAssociatedPaymentLevel.getOrElse(""))
        .now

    if (hiwiOption.isEmpty || paymentLevelOption.isEmpty) {
      promise.failure(new Exception("The PDF could not be created because not all required fields are filled in!"))
    }

    val hiwi = hiwiOption.get.signal.now
    val paymentLevel = paymentLevelOption.get.signal.now

    PDF.fill(
      url,
      Seq(
        PDFTextField(
          "Vorname Nachname (Studentische Hilfskraft)",
          s"${hiwi.firstName.getOrElse("")} ${hiwi.lastName.getOrElse("")}",
        ),
        PDFTextField(
          "Geburtsdatum (Studentische Hilfskraft)",
          s"${toGermanDate(hiwi.birthdate.getOrElse(0))}",
        ),
        PDFTextField(
          "Vertragsbeginn",
          toGermanDate(contract.contractStartDate.getOrElse(0)),
        ),
        PDFTextField(
          "Vertragsende",
          toGermanDate(contract.contractEndDate.getOrElse(0)),
        ),
        PDFTextField(
          "Arbeitszeit Kästchen 1",
          contract.contractHoursPerMonth.getOrElse(0).toString,
        ),
        PDFCheckboxField("Arbeitszeit Kontrollkästchen 1", true),
        PDFCheckboxField(
          paymentLevel.pdfCheckboxName.getOrElse(""),
          true,
        ),
      ),
    )
  }

  def render: VMod
}

class BasicInformation(
    existingId: String,
    editingValue: Var[Contract],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
    extend: Boolean = false,
)(using
    jsImplicits: JSImplicits,
) extends Step("Basic Information", existingId, editingValue, disabled, disabledDescription) {
  def render: VMod = this.editStep(
    div(
      cls := "p-4 space-y-4",
      div(
        cls := "flex flex-col md:flex-row md:space-x-4",
        div(
          cls := "basis-1/2",
          label(cls := "font-bold", "Hiwi:"),
          ContractPageAttributes().contractAssociatedHiwi.renderEdit("", editingValue, cls := "rounded-md"),
          span(
            cls := "text-slate-400 dark:text-gray-400 italic text-xs",
            Signal.dynamic {
              jsImplicits.repositories.hiwis
                .find(editingValue.value.contractAssociatedHiwi.getOrElse(""))
                .value
                .flatMap(hiwi => hiwi.signal.value.eMail.option)
            },
          ),
        ),
        div(
          cls := "basis-1/2",
          label(cls := "font-bold", "Supervisor:"),
          ContractPageAttributes().contractAssociatedSupervisor.renderEdit("", editingValue, cls := "rounded-md"),
          span(
            cls := "text-slate-400 dark:text-gray-400 dark:text-gray-400 italic text-xs",
            Signal.dynamic {
              jsImplicits.repositories.supervisors
                .find(editingValue.value.contractAssociatedSupervisor.getOrElse(""))
                .value
                .flatMap(supervisor => supervisor.signal.value.eMail.option)
            },
          ),
        ),
      ),
      div(
        cls := "flex flex-col md:flex-row md:space-x-4",
        div(
          cls := "basis-2/5",
          label(cls := "font-bold", "Start date:"),
          ContractPageAttributes().contractStartDate.renderEdit("", editingValue, cls := "!rounded-md"),
          Signal.dynamic {
            val contract = editingValue.value
            if (
              contract.contractStartDate.hasValue && dateDiffDays(
                js.Date.now().toLong,
                contract.contractStartDate.get,
              ) < 0
            ) {
              Some(
                p(
                  cls := "bg-yellow-100 text-yellow-600 flex flex-row p-4 rounded-md gap-2 mt-2 text-sm",
                  icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
                  "Start date is in the past",
                ),
              )
            } else None
          },
        ),
        div(
          cls := "basis-1/5",
          label(cls := "font-bold", "Duration: "),
          br,
          Signal.dynamic {
            val contract = editingValue.value

            if (
              contract.contractStartDate.option.nonEmpty && contract.contractEndDate.option.nonEmpty && !(dateDiffDays(
                contract.contractStartDate.getOrElse(0L),
                contract.contractEndDate.getOrElse(0L),
              ) < 0 ||
                dateDiffDays(js.Date.now().toLong, contract.contractEndDate.getOrElse(0L)) < 0)
            ) {
              Some(
                dateDiffHumanReadable(
                  contract.contractStartDate.getOrElse(0L),
                  contract.contractEndDate.getOrElse(0L),
                ),
              )
            } else None
          },
        ),
        div(
          cls := "basis-2/5",
          label(cls := "font-bold", "End date:"),
          ContractPageAttributes().contractEndDate.renderEdit("", editingValue, cls := "!rounded-md"),
          Signal.dynamic {
            val contract = editingValue.value
            if (
              contract.contractEndDate.option.nonEmpty && (dateDiffDays(
                contract.contractStartDate.getOrElse(0L),
                contract.contractEndDate.getOrElse(0L),
              ) < 0 || dateDiffDays(
                js.Date.now().toLong,
                contract.contractEndDate.getOrElse(0L),
              ) < 0)
            ) {
              Some(
                p(
                  cls := "bg-yellow-100 text-yellow-600 flex flex-row p-4 rounded-md gap-2 mt-2 text-sm",
                  icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
                  "End date is in the past or before start date",
                ),
              )
            } else None
          },
        ),
      ),
      Signal.dynamic {
        val overlappingContract = {
          val start = editingValue.value.contractStartDate.getOrElse(0L)
          val end = editingValue.value.contractEndDate.getOrElse(0L)
          val hiwi = editingValue.value.contractAssociatedHiwi.getOrElse("")
          val overlappingContracts = jsImplicits.repositories.contracts.existing.value.filter(c => {
            val contract = c.signal.value
            !contract.isDraft.getOrElse(true) &&
            contract.contractAssociatedHiwi.option.nonEmpty &&
            contract.contractAssociatedHiwi.option.get == hiwi &&
            contract.contractStartDate.option.nonEmpty &&
            contract.contractEndDate.option.nonEmpty &&
            (
              (
                contract.contractStartDate.getOrElse(0L) <= start &&
                  contract.contractEndDate.getOrElse(0L) >= start
              ) ||
                (
                  (contract.contractStartDate.getOrElse(0L) <= end) &&
                    contract.contractEndDate.getOrElse(0L) >= end
                ) ||
                (
                  contract.contractStartDate.getOrElse(0L) >= start &&
                    contract.contractEndDate.getOrElse(0L) <= end
                )
            )
          })

          overlappingContracts.nonEmpty
        }

        if (overlappingContract) {
          Some(
            p(
              cls := "bg-yellow-100 text-yellow-600 flex flex-row p-4 rounded-md gap-2 mt-2 text-sm",
              icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
              "This hiwi already has a finalized contract that overlaps with your selected contract period.",
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
              val contract = editingValue.value
              val limit =
                ContractPageAttributes()
                  .getLimit(existingId, contract, contract.contractStartDate.getOrElse(0L))
                  .value
              val hourlyWage =
                ContractPageAttributes()
                  .getMoneyPerHour(existingId, contract, contract.contractStartDate.getOrElse(0L))
                  .value

              val month = dateDiffMonth(
                contract.contractStartDate.getOrElse(0L),
                contract.contractEndDate.getOrElse(0L),
              )

              if (hourlyWage > BigDecimal(0) && limit > BigDecimal(0)) {
                val maxHours = (limit / hourlyWage).setScale(0, RoundingMode.FLOOR)
                div(
                  cls := "flex flex-col gap-2",
                  div(
                    cls := "flex flex-col gap-1",
                    span(
                      span(cls := "text-sm text-slate-600 dark:text-gray-200", "Base Salary: "),
                      span(
                        cls := "font-bold",
                        toMoneyString(contract.contractHoursPerMonth.getOrElse(0) * hourlyWage),
                      ),
                    ),
                    span(
                      cls := "text-slate-400 dark:text-gray-400 text-xs italic",
                      s"calculating with a salary of ${toMoneyString(hourlyWage)}/h that was set when the contract has started",
                    ),
                  ),
                  div(
                    cls := "flex flex-col gap-1 dark:text-gray-200",
                    span(
                      span(cls := "text-sm text-slate-600 dark:text-gray-200", "Minijob Limit: "),
                      span(cls := "font-bold", toMoneyString(limit)),
                    ),
                    span(
                      cls := "text-slate-400 dark:text-gray-400 text-xs italic",
                      "the limit when the contract has started",
                    ),
                  ),
                  div(
                    cls := "flex flex-col gap-1 dark:text-gray-200",
                    span(
                      span(cls := "text-sm text-slate-600 dark:text-gray-200", "Maximum Hours below Limit: "),
                      span(cls := "font-bold", maxHours.toInt),
                      span(
                        "use",
                        cls := "underline py-1 px-2 bg-purple-200 text-purple-600 text-xs rounded-md ml-2 cursor-pointer",
                        onClick.foreach(_ => {
                          this.updateHoursPerMonth(maxHours.toInt)
                        }),
                      ),
                    ),
                  ),
                  if (
                    contract.contractStartDate.option.nonEmpty && contract.contractEndDate.option.nonEmpty && month >= 0
                  ) {
                    Some(
                      div(
                        cls := "flex flex-col gap-1",
                        span(
                          span(cls := "text-sm text-slate-600 dark:text-gray-200", "Total hours: "),
                          span(cls := "font-bold", contract.contractHoursPerMonth.getOrElse(0) * month),
                        ),
                        span(
                          cls := "text-slate-400 dark:text-gray-400 text-xs italic",
                          s"calculating with $month month",
                        ),
                      ),
                    )
                  } else None,
                )
              } else {
                span()
              }
            },
          ),
        ),
        div(
          cls := "basis-1/2",
          label(cls := "font-bold", "Payment Level:"),
          ContractPageAttributes().contractAssociatedPaymentLevel.renderEdit("", editingValue, cls := "rounded-md"),
          label(cls := "font-bold", "Hours per month:"),
          ContractPageAttributes().contractHoursPerMonth.renderEdit("", editingValue, cls := "!rounded-md"),
          Signal.dynamic {
            val contract = editingValue.value
            val project = contract.contractAssociatedProject.option.flatMap(id =>
              jsImplicits.repositories.projects.all.value
                .find(project => project.id == id),
            )
            val limit =
              ContractPageAttributes()
                .getLimit(existingId, contract, contract.contractStartDate.getOrElse(0L))
                .value
            val hourlyWage = ContractPageAttributes()
              .getMoneyPerHour(existingId, contract, contract.contractStartDate.getOrElse(0L))
              .value
            val maxHoursForTax =
              if (hourlyWage > BigDecimal(0)) (limit / hourlyWage).setScale(0, RoundingMode.FLOOR)
              else {
                BigDecimal(0)
              }
            val month = dateDiffMonth(
              contract.contractStartDate.getOrElse(0L),
              contract.contractEndDate.getOrElse(0L),
            )

            val totalHoursWithoutThisContract = project.map(project =>
              ProjectAttributes()
                .countContractHours(
                  contract.contractAssociatedProject.getOrElse(""),
                  project.signal.value,
                  (id, contract) => !contract.isDraft.getOrElse(true) && id != existingId,
                )
                .value +
                ProjectAttributes()
                  .countContractHours(
                    contract.contractAssociatedProject.getOrElse(""),
                    project.signal.value,
                    (id, contract) => contract.isDraft.getOrElse(true) && id != existingId,
                  )
                  .value,
            )

            val maxHoursForProject = project.flatMap(project => {
              if (month == 0) None
              else
                Some(
                  (project.signal.value.maxHours.getOrElse(0) - totalHoursWithoutThisContract.getOrElse(
                    0,
                  )) / month,
                )
            })

            val hours = BigDecimal(contract.contractHoursPerMonth.getOrElse(0))
            if (hours > maxHoursForTax && limit != 0) {
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
                    s"reduce the hours to $maxHoursForTax hours.",
                  ),
                ),
              )
            } else if (
              project.nonEmpty
              && hours * month + totalHoursWithoutThisContract.getOrElse(0) > project.get.signal.value.maxHours
                .getOrElse(0)
              && !extend
              && maxHoursForProject.nonEmpty
            ) {
              p(
                cls := "bg-yellow-100 text-yellow-600 flex flex-row p-2 rounded-lg gap-2 mt-2 text-sm",
                icons.WarningTriangle(cls := "w-6 h-6 shrink-0"),
                span(
                  "Together with the other contracts and contract drafts assigned to this project the maximum amount of hours is exceeded! ",
                  span(
                    cls := "underline cursor-pointer",
                    onClick.foreach(_ => {
                      this.updateHoursPerMonth(
                        if (maxHoursForProject.get < 0) 0
                        else maxHoursForProject.get,
                      )
                    }),
                    s"You might want to reduce the monthly hours to ${if (maxHoursForProject.get < 0) 0
                      else maxHoursForProject.get}.",
                  ),
                ),
              )
            } else {
              p()
            }
          },
        ),
      ),
    ),
  )
}

class SelectProject(
    existingId: String,
    editingValue: Var[Contract],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("Select Project", existingId, editingValue, disabled, disabledDescription) {
  def render: VMod = {
    this.editStep(
      div(
        cls := "flex flex-col md:flex-row p-4 md:space-x-4",
        div(
          cls := "basis-1/2",
          label(cls := "font-bold", "Project:"),
          ContractPageAttributes().contractAssociatedProject.renderEdit("", editingValue, cls := "rounded-md"),
        ),
        div(
          cls := "basis-[12.5%] flex flex-row md:flex-col justify-between",
          label(
            cls := "font-bold",
            Signal.dynamic {
              if (editingValue.value.isDraft.getOrElse(true))
                "Contracts:"
              else
                "Other Contracts:"
            },
          ),
          div(
            Signal.dynamic {
              editingValue.value.contractAssociatedProject.option.flatMap(id =>
                jsImplicits.repositories.projects.all.value
                  .find(project => project.id == id)
                  .map(project =>
                    ProjectAttributes()
                      .countContractHours(
                        id,
                        project.signal.value,
                        (id, contract) => !contract.isDraft.getOrElse(true) && id != existingId,
                      )
                      .value
                      .toString + " h",
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
              if (editingValue.value.isDraft.getOrElse(true))
                "Other Drafts:"
              else
                "Drafts:"
            },
          ),
          div(
            Signal.dynamic {
              editingValue.value.contractAssociatedProject.option.flatMap(id =>
                jsImplicits.repositories.projects.all.value
                  .find(project => project.id == id)
                  .map(project =>
                    ProjectAttributes()
                      .countContractHours(
                        id,
                        project.signal.value,
                        (id, contract) => contract.isDraft.getOrElse(true) && id != existingId,
                      )
                      .value
                      .toString + " h",
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
              if (editingValue.value.isDraft.getOrElse(true))
                "This Draft:"
              else
                "This Contract:"
            },
          ),
          div(
            Signal.dynamic {
              val contract = editingValue.value
              if (contract.contractEndDate.option.nonEmpty && contract.contractStartDate.option.nonEmpty) {
                s"${ContractPageAttributes().getTotalHours(existingId, contract)} h"
              } else ""
            },
          ),
        ),
        div(
          cls := "basis-[12.5%] flex flex-row md:flex-col justify-between",
          label(cls := "font-bold", "Max. hours"),
          div(Signal.dynamic {
            val contract = editingValue.value
            contract.contractAssociatedProject.option.flatMap(id =>
              jsImplicits.repositories.projects
                .find(id)
                .value
                .map(project => s"${project.signal.value.maxHours.getOrElse(0)} h"),
            )
          }),
        ),
      ),
    )
  }
}

class SelectContractSchema(
    existingId: String,
    editingValue: Var[Contract],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("Contract Schema", existingId, editingValue, disabled, disabledDescription) {
  def render: VMod = {
    this.editStep(
      div(
        cls := "p-4",
        label(cls := "font-bold", "Contract schema:"),
        ContractPageAttributes().contractSchema.renderEdit("", editingValue, cls := "rounded-md"),
      ),
    )
  }
}

class ContractRequirements(
    existingId: String,
    editingValue: Var[Contract],
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("Contract requirements", existingId, editingValue, disabled, disabledDescription) {
  def render: VMod = {
    this.editStep(
      div(
        cls := "p-4",
        "Check all forms the hiwi has filled out and handed back.",
        ContractPageAttributes().requiredDocuments.renderEdit("", editingValue, cls := "rounded-md"),
        i(
          cls := "text-slate-400 dark:text-gray-400 text-xs",
          "Documents written in italic have been checked in an older contract type and will be removed from this list once unchecked.",
        ),
      ),
    )
  }
}

class ContractRequirementsMail(
    existingId: String,
    editContract: Var[Contract],
    save: () => Unit,
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step(
      "Contract requirements - reminder mail",
      existingId,
      editContract,
      disabled,
      disabledDescription,
    ) {
  def render: VMod = {
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

            val contract = editContract.now

            val supervisorOption = jsImplicits.repositories.supervisors.all.now.find(p =>
              p.id == contract.contractAssociatedSupervisor.getOrElse(""),
            )
            val hiwiOption =
              jsImplicits.repositories.hiwis.all.now.find(p => p.id == contract.contractAssociatedHiwi.getOrElse(""))

            val contractTypeOption =
              jsImplicits.repositories.contractSchemas.all.now.find(p => p.id == contract.contractSchema.getOrElse(""))

            val documents = jsImplicits.repositories.documents.all.now

            if (hiwiOption.nonEmpty && supervisorOption.nonEmpty && contractTypeOption.nonEmpty) {
              val hiwi = hiwiOption.get.signal.now
              val supervisor = supervisorOption.get.signal.now
              val neededDocuments = contractTypeOption.get.signal.now.files.option
                .getOrElse(Seq.empty)
                .filter(p => !contract.requiredDocuments.getOrElse(Seq.empty).contains(p))
                .map(documentId =>
                  documents
                    .find(p => p.id == documentId)
                    .map(document => document.signal.now.name.getOrElse(""))
                    .getOrElse(""),
                )

              jsImplicits.mailing
                .sendMail(
                  hiwi.eMail.getOrElse(""),
                  supervisor.eMail.getOrElse(""),
                  supervisor.name.getOrElse(""),
                  ReminderMail(
                    hiwi,
                    supervisor,
                    (js.Date.now + 12096e5).toLong, // magic Number is 14 days in ms
                    neededDocuments,
                  ),
                  Seq(supervisor.eMail.getOrElse("")),
                )
                .onComplete({
                  case Failure(e) =>
                    println(e)
                    jsImplicits.toaster.error("Sending mail")
                  case Success(ans) =>
                    document.querySelector("#sendReminder").classList.remove("loading")
                    if (ans.rejected.length > 0) {
                      jsImplicits.toaster.warn(s"Could not deliver mail to ${ans.rejected.mkString(", ")}.")
                    }
                    if (ans.accepted.length > 0) {
                      editContract.transform(_.copy(reminderSentDate = Attribute(js.Date.now.toLong)))
                      save()
                      jsImplicits.toaster.make(s"Sent mail to ${ans.accepted.mkString(" and ")}.")
                    }
                })
            }

          }),
        ),
        div(
          cls := "text-xs text-slate-400 dark:text-gray-400 italic",
          "Last sent: ",
          span(
            cls := "bg-purple-200 py-1 px-2 rounded-md text-purple-600",
            Signal.dynamic {
              val date = editContract.value.reminderSentDate.getOrElse(0L)
              if (date > 0L) toGermanDate(date) else "Never"
            },
          ),
        ),
      ),
    )
  }
}

class CreateHiwiDocuments(
    existingId: String,
    editingValue: Var[Contract],
    save: () => Unit,
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("Create Documents for Hiwi", existingId, editingValue, disabled, disabledDescription) {
  def render: VMod = {
    this.editStep(
      div(
        cls := "p-4 flex flex-col",
        div(
          cls := "flex gap-2",
          Button(
            ButtonStyle.LightPrimary,
            "Create Documents",
            idAttr := "loadDocuments",
            onClick.foreach(e => {
              e.preventDefault()
              document.getElementById("loadDocuments").classList.add("loading")
              this
                .fillContractPDF(Globals.VITE_CONTRACT_PDF_PATH)
                .toastOnError()
                .onComplete(v => {
                  document.getElementById("loadDocuments").classList.remove("loading")
                  if (v.isSuccess) {
                    val buffer = v.get
                    PDF.download("contract.pdf", buffer)
                  }
                })
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

                val contract = editingValue.now

                val supervisorOption = jsImplicits.repositories.supervisors.all.now.find(p =>
                  p.id == contract.contractAssociatedSupervisor.getOrElse(""),
                )
                val hiwiOption =
                  jsImplicits.repositories.hiwis.all.now.find(p =>
                    p.id == contract.contractAssociatedHiwi.getOrElse(""),
                  )

                if (hiwiOption.nonEmpty && supervisorOption.nonEmpty) {
                  val hiwi = hiwiOption.get.signal.now
                  val supervisor = supervisorOption.get.signal.now

                  this
                    .fillContractPDF(Globals.VITE_CONTRACT_PDF_PATH)
                    .toastOnError()
                    .onComplete(contract => {
                      jsImplicits.mailing
                        .sendMail(
                          hiwi.eMail.getOrElse(""),
                          supervisor.eMail.getOrElse(""),
                          supervisor.name.getOrElse(""),
                          ContractEmail(
                            hiwi,
                            supervisor,
                            (js.Date.now + 12096e5).toLong, // magic Number is 14 days in ms
                            contract.get,
                          ),
                          Seq(supervisor.eMail.getOrElse("")),
                        )
                        .andThen(ans => {
                          document.querySelector("#sendContract").classList.remove("loading")
                          if (ans.get.rejected.length > 0) {
                            jsImplicits.toaster
                              .make(s"Could not deliver mail to ${ans.get.rejected.mkString(" and ")}.")
                          }
                          if (ans.get.accepted.length > 0) {
                            editingValue.transform(_.copy(contractSentDate = Attribute(js.Date.now.toLong)))
                            save()
                            jsImplicits.toaster.make(s"Sent mail to ${ans.get.accepted.mkString(" and ")}.")
                          }
                        })
                        .toastOnError()
                    })
                }
              }),
            ),
            div(
              cls := "text-xs text-slate-400 dark:text-gray-400 italic",
              "Last sent: ",
              span(
                cls := "bg-purple-200 py-1 px-2 rounded-md text-purple-600",
                Signal.dynamic {
                  val date = editingValue.value.contractSentDate.getOrElse(0L)
                  if (date > 0L) toGermanDate(date) else "Never"
                },
              ),
            ),
          ),
        ),
        label(
          ContractPageAttributes().signed.renderEdit("", editingValue, cls := "rounded-md"),
          span(" The contract has been signed"),
          cls := "mt-2 flex gap-2 items-center",
        ),
      ),
    )
  }

  def generateDocuments: Future[Seq[ArrayBuffer[Short]]] = ???
}

class CreateLetter(
    existingId: String,
    editingValue: Var[Contract],
    save: () => Unit,
    disabled: Signal[Seq[(Boolean, String)]] = Signal(Seq.empty),
    disabledDescription: String = "",
)(using
    jsImplicits: JSImplicits,
) extends Step("Letter to Dekanat", existingId, editingValue, disabled, disabledDescription) {
  def render: VMod = {
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
              this
                .fillLetterPDF(Globals.VITE_LETTER_PDF_PATH)
                .toastOnError()
                .onComplete(v => {
                  document.getElementById("loadLetter").classList.remove("loading")
                  if (v.isSuccess) {
                    val buffer = v.get
                    PDF.download("letter.pdf", buffer)
                  }
                })
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

                val contract = editingValue.now

                val supervisorOption = jsImplicits.repositories.supervisors.all.now.find(p =>
                  p.id == contract.contractAssociatedSupervisor.getOrElse(""),
                )
                val hiwiOption =
                  jsImplicits.repositories.hiwis.all.now.find(p =>
                    p.id == contract.contractAssociatedHiwi.getOrElse(""),
                  )

                if (hiwiOption.nonEmpty && supervisorOption.nonEmpty) {
                  val hiwi = hiwiOption.get.signal.now
                  val supervisor = supervisorOption.get.signal.now

                  this
                    .fillLetterPDF(Globals.VITE_LETTER_PDF_PATH)
                    .toastOnError()
                    .onComplete(letter => {
                      jsImplicits.mailing
                        .sendMail(
                          Globals.VITE_DEKANAT_MAIL,
                          supervisor.eMail.getOrElse(""),
                          supervisor.name.getOrElse(""),
                          DekanatMail(
                            hiwi,
                            supervisor,
                            letter.get,
                          ),
                          Seq(supervisor.eMail.getOrElse("")),
                        )
                        .andThen(ans => {
                          document.querySelector("#sendLetter").classList.remove("loading")
                          if (ans.get.rejected.length > 0) {
                            jsImplicits.toaster
                              .make(s"Could not deliver mail to ${ans.get.rejected.mkString(" and ")}.")
                          }
                          if (ans.get.accepted.length > 0) {
                            editingValue.transform(_.copy(letterSentDate = Attribute(js.Date.now.toLong)))
                            save()
                            jsImplicits.toaster.make(s"Sent mail to ${ans.get.accepted.mkString(" and ")}.")
                          }
                        })
                        .toastOnError()
                    })
                }
              }),
            ),
            div(
              cls := "text-xs text-slate-400 dark:text-gray-400 italic",
              "Last sent: ",
              span(
                cls := "bg-purple-200 py-1 px-2 rounded-md text-purple-600",
                Signal.dynamic {
                  val date = editingValue.value.letterSentDate.getOrElse(0L)
                  if (date > 0L) toGermanDate(date) else "Never"
                },
              ),
            ),
          ),
        ),
        label(
          ContractPageAttributes().submitted.renderEdit("", editingValue, cls := "rounded-md"),
          span(" The letter has been submitted"),
          cls := "mt-2 flex gap-2 items-center",
        ),
      ),
    )
  }
}

class InnerExtendContractsPage(override val existingValue: Synced[Contract], override val contractId: String)(using
    jsImplicits: JSImplicits,
) extends InnerEditContractsPage(existingValue, contractId) {

  private def createDraft(): Future[String] = {
    jsImplicits.indexeddb.requestPersistentStorage()

    val editingNow = editContract.now
    val draftContract = Contract(
      contractAssociatedProject = editingNow.contractAssociatedProject,
      contractAssociatedHiwi = editingNow.contractAssociatedHiwi,
      contractAssociatedPaymentLevel = editingNow.contractAssociatedPaymentLevel,
      contractAssociatedSupervisor = editingNow.contractAssociatedSupervisor,
      contractStartDate = editingNow.contractStartDate,
      contractEndDate = editingNow.contractEndDate,
      contractHoursPerMonth = editingNow.contractHoursPerMonth,
      contractSchema = editingNow.contractSchema,
      isDraft = Attribute(true),
    )
    jsImplicits.repositories.contracts
      .create(draftContract)
      .map(entity => {
        editContract.set(entity.signal.now)
        jsImplicits.routing.to(EditContractsPage(entity.id))
        entity.id
      })
  }

  override def render: VMod = {
    div(
      cls := "flex flex-col items-center",
      div(
        cls := "p-1",
        h1(
          cls := "text-3xl mt-4 text-center",
          "Extend Contract",
        ),
      ),
      div(
        cls := "relative md:shadow-md md:rounded-lg py-4 px-0 md:px-4 my-4 inline-block overflow-y-visible max-w-[900px] w-[95%]",
        form(
          BasicInformation(contractId, editContract, Signal(Seq.empty), "", true).render,
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
    )
  }
}

class InnerEditContractsPage(val existingValue: Synced[Contract], val contractId: String)(using
    jsImplicits: JSImplicits,
) {

  private def isCompleted: Signal[Boolean] = {
    Signal.dynamic {
      val contract = editContract.value
      val requiredDocuments = jsImplicits.repositories.contractSchemas.all.value
        .find(s => s.id == contract.contractSchema.getOrElse(""))
        .flatMap(t => t.signal.value.files.option)

      contract.contractAssociatedHiwi.option.isEmpty ||
      contract.contractAssociatedSupervisor.option.isEmpty ||
      contract.contractStartDate.option.isEmpty ||
      contract.contractEndDate.option.isEmpty ||
      contract.contractAssociatedProject.option.isEmpty ||
      contract.contractHoursPerMonth.option.isEmpty ||
      contract.contractHoursPerMonth.getOrElse(0) < 0 ||
      contract.contractAssociatedPaymentLevel.option.isEmpty ||
      dateDiffDays(contract.contractStartDate.getOrElse(0L), contract.contractEndDate.getOrElse(0L)) < 0 ||
      dateDiffDays(js.Date.now().toLong, contract.contractEndDate.getOrElse(0L)) < 0 ||
      contract.requiredDocuments.option.isEmpty ||
      !contract.isSigned.getOrElse(false) ||
      !requiredDocuments
        .getOrElse(Seq.empty)
        .forall(id => contract.requiredDocuments.getOrElse(Seq.empty).contains(id))
    }
  }

  protected def createOrUpdate(
      finalize: Boolean = false,
      stayOnPage: Boolean = false,
      silent: Boolean = false,
  ): Future[String] = {
    jsImplicits.indexeddb.requestPersistentStorage()

    val editingNow = editContract.now
    existingValue
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
          if (value.isDraft.getOrElse(true)) {
            jsImplicits.routing.to(ContractDraftsPage())
          } else {
            jsImplicits.routing.to(ContractsPage())
          }
        }

        existingValue.id
      })
  }

  protected def cancelEdit(): Unit = Signal.dynamic {
    if (editContract.value.isDraft.getOrElse(true)) {
      jsImplicits.routing.to(ContractDraftsPage())
    } else {
      jsImplicits.routing.to(ContractsPage())
    }
  }

  var editContract: Var[Contract] = Var(existingValue.signal.now)

  private val isDirty: Signal[Boolean] = Signal.dynamic {
    editContract.value != existingValue.signal.value
  }

  private val actions: Seq[VNode] = Seq(
    Button(
      ButtonStyle.LightPrimary,
      "Save",
      Signal.dynamic {
        if (isDirty.value) span(cls := "inline-block ml-1", icons.Circle(cls := "w-3 h-3"))
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
        createOrUpdate(finalize = true).toastOnError(ToastMode.Infinit)
      }),
      disabled <-- isCompleted,
    ),
    Button(ButtonStyle.LightDefault, "Cancel", onClick.foreach(_ => cancelEdit())),
  )

  private val mobileActions: Seq[VNode] = Seq(
    Button(
      ButtonStyle.LightPrimary,
      cls := "min-h-8",
      Signal.dynamic {
        if (isDirty.value) span(cls := "inline-block", icons.Save(cls := "w-4 h-4"))
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
      icons.Return(cls := "w-4 h-4"),
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
      disabled <-- isCompleted,
    ),
    Button(
      ButtonStyle.LightDefault,
      cls := "min-h-8",
      icons.Close(cls := "w-4 h-4"),
      onClick.foreach(_ => cancelEdit()),
    ),
  )

  private val ctrlSListener: js.Function1[KeyboardEvent, Unit] = (e: KeyboardEvent) => {
    if (e.keyCode == 83 && e.ctrlKey) {
      e.preventDefault()
      createOrUpdate(false, true)
        .map(id => jsImplicits.routing.to(EditContractsPage(id), false, Map.empty, true))
        .toastOnError(ToastMode.Infinit)
    }
  }

  private val unloadListener: js.Function1[BeforeUnloadEvent, String] = (e: BeforeUnloadEvent) => {
    e.preventDefault()
    e.returnValue = ""
    ""
  }

  private def markUnloadListener: VMod = Signal.dynamic {
    if (isDirty.value) window.addEventListener("beforeunload", unloadListener)
    else window.removeEventListener("beforeunload", unloadListener)
    ""
  }

  def render: VMod = {
  div(
  //   markUnloadListener,
  //   onDomMount.foreach(_ => document.addEventListener("keydown", ctrlSListener)),
  //   onDomUnmount.foreach(_ => {
  //     document.removeEventListener("keydown", ctrlSListener)
  //     window.removeEventListener("beforeunload", unloadListener)
  //   }),
  //   div(
  //     cls := "flex flex-col items-center",
  //     div(
  //       cls := "p-1",
  //       h1(
  //         cls := "text-3xl mt-4 text-center",
  //         Signal.dynamic {
  //           if (editContract.value.isDraft.getOrElse(true))
  //             "Edit Contract Draft"
  //           else
  //             "Edit Contract"
  //         },
  //       ),
  //     ),
       div(
         cls := "relative md:shadow-md rounded-lg py-4 px-0 md:px-4 my-4 inline-block overflow-y-visible max-w-[900px] w-[95%]",
         form(
  //         BasicInformation(contractId, editContract).render,
  //         SelectProject(contractId, editContract).render,
           SelectContractSchema(contractId, editContract).render,
  //         ContractRequirements(contractId, editContract).render,
  //         ContractRequirementsMail(
  //           contractId,
  //           editContract,
  //           () => createOrUpdate(stayOnPage = true),
  //           Signal.dynamic {
  //             val contract = editContract.value
  //             val requiredDocuments: Seq[String] = contractDocuments.getDocumentsFromContractSchema(contract).value
  //             Seq(
  //               contract.contractAssociatedHiwi.option.isEmpty -> "a hiwi",
  //               contract.contractAssociatedSupervisor.option.isEmpty -> "a supervisor",
  //               contract.contractSchema.option.isEmpty -> "a contract schema",
  //               !jsImplicits.discovery.online.value -> "to be connected to the discovery server",
  //               (contract.contractSchema.hasValue && requiredDocuments.isEmpty) -> "at least one requirement that can be checked",
  //               (contract.contractSchema.hasValue && requiredDocuments
  //                 .forall(id =>
  //                   contract.requiredDocuments.getOrElse(Seq.empty).contains(id),
  //                 )) -> "at least one requirement that has not been checked",
  //             )
  //           },
  //           "send a reminder email",
  //         ).render,
  //         CreateHiwiDocuments(
  //           contractId,
  //           editContract,
  //           () => createOrUpdate(false, true, true),
  //           Signal.dynamic {
  //             val contract = editContract.value
  //             val hiwi =
  //               contract.contractAssociatedHiwi.option
  //                 .flatMap(id => jsImplicits.repositories.hiwis.find(id).value)
  //             Seq(
  //               hiwi.isEmpty -> "a hiwi",
  //               contract.contractEndDate.option.isEmpty -> "an end date",
  //               contract.contractStartDate.option.isEmpty -> "a start date",
  //               contract.contractSchema.option.isEmpty -> "a contract schema",
  //               contract.contractAssociatedPaymentLevel.option.isEmpty -> "a payment level",
  //               hiwi.flatMap(_.signal.value.birthdate.option).isEmpty -> "a hiwi with a date of birth",
  //               contract.contractHoursPerMonth.option.isEmpty -> "to set hours per month",
  //             )
  //           },
  //           "create and download documents",
  //         ).render,
  //         CreateLetter(
  //           contractId,
  //           editContract,
  //           () => createOrUpdate(false, true, true),
  //           Signal.dynamic {
  //             val contract = editContract.value
  //             Seq(
  //               contract.contractAssociatedHiwi.option.isEmpty -> "a hiwi",
  //               contract.contractEndDate.option.isEmpty -> "an end date",
  //               contract.contractStartDate.option.isEmpty -> "a start date",
  //               contract.contractAssociatedPaymentLevel.option.isEmpty -> "a payment level",
  //               contract.contractHoursPerMonth.option.isEmpty -> "to set hours per month",
  //               contract.contractAssociatedProject.option.isEmpty -> "a project",
  //               !contract.isSigned.getOrElse(false) -> "a signed contract",
  //             )
  //           },
  //           "create and download a letter",
  //         ).render,
  //         div(
  //           idAttr := "static_buttons",
  //           cls := "pl-4 flex flex-col md:flex-row gap-2",
  //           actions,
  //         ),
         ),
       ),
  //     div(
  //       idAttr := "sticky_buttons",
  //       onDomMount.foreach(_ => stickyButton("#static_buttons", "#sticky_buttons", "hidden")),
  //       onDomUnmount.foreach(_ => cleanStickyButtons()),
  //       cls := "left-4 md:space-x-4 fixed bottom-4 p-3 bg-slate-50/75 dark:bg-gray-500/75 dark:border-gray-500 shadow-lg rounded-xl border border-slate-200 hidden z-[200]",
  //       div(cls := "flex-row gap-2 hidden md:flex", actions),
  //       div(cls := "flex flex-row gap-2 md:hidden", mobileActions),
  //     ),
  //   ),
  )
  }

}

class ContractDocuments(using jsImplicits: JSImplicits) {

  def getDocumentsFromContractSchema(contract: Contract): Signal[Seq[String]] = Signal.dynamic {
    contract.contractSchema.option.flatMap({ schema =>
        jsImplicits.repositories.contractSchemas
          .find(schema)
          .value
          .flatMap(_.signal.value.files.option)
      })
      .getOrElse(Seq.empty)
  }

}