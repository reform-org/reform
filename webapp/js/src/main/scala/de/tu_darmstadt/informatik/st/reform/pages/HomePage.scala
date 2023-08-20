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
import de.tu_darmstadt.informatik.st.reform.components.*
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.entity.Contract
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.toGermanDate
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.toHumanMonth
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.toMoneyString
import de.tu_darmstadt.informatik.st.reform.npm.*
import de.tu_darmstadt.informatik.st.reform.services.Page
import de.tu_darmstadt.informatik.st.reform.{*, given}
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

import scala.scalajs.js

case class HomePage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def getContractsForInterval(month: Int, year: Int, pred: (String, Contract) => Boolean = (_, _) => true) = {
    Signal.dynamic {
      jsImplicits.repositories.contracts.existing.value
        .map(p => (p.id -> p.signal.value))
        .filter((id, p) => pred(id, p) && ContractPageAttributes().isInInterval(p, month, year))
    }
  }

  def render: VMod = {
    val year =
      jsImplicits.routing
        .getQueryParameterAsString("year")
        .map(p => if (p == "") new js.Date().getFullYear().toInt else p.toInt)
    val month =
      jsImplicits.routing
        .getQueryParameterAsString("month")
        .map(p => if (p == "") new js.Date().getMonth().toInt else p.toInt)

    div(
      cls := "flex flex-col gap-4 w-full",
      div(
        cls := "flex flex-row gap-4 items-center self-center",
        IconButton(
          ButtonStyle.LightDefault,
          icons.Previous(cls := "w-6 h-6"),
          onClick.foreach(_ => {
            val m = month.now
            val y = year.now
            jsImplicits.routing.updateQueryParameters(
              Map(
                ("month" -> (if (m == 1) "12" else (m - 1).toString)),
                ("year" -> (if (m == 1) (y - 1).toString else y.toString)),
              ),
            )
          }),
        ),
        div(cls := "min-w-[120px] text-center", Signal { toHumanMonth(month.value) }, " ", year),
        IconButton(
          ButtonStyle.LightDefault,
          icons.Next(cls := "w-6 h-6"),
          onClick.foreach(_ => {
            val m = month.now
            val y = year.now
            jsImplicits.routing.updateQueryParameters(
              Map(
                ("month" -> (if (m == 12) "1" else (m + 1).toString)),
                ("year" -> (if (m == 12) (y + 1).toString else y.toString)),
              ),
            )
          }),
        ),
      ),
      div(
        cls := "flex flex-wrap gap-4 items-stretch justify-center",
        NumberCard(
          "Total Contracts",
          Signal.dynamic {
            getContractsForInterval(month.value, year.value).map(a => a.size)
          },
          "all contracts",
        ),
        NumberCard(
          "Draft Contracts:",
          Signal.dynamic {
            getContractsForInterval(month.value, year.value, (_, p) => p.isDraft.get.getOrElse(true)).map(a => a.size)
          },
          "only draft contracts",
        ),
        NumberCard(
          "Contracts:",
          Signal.dynamic {
            getContractsForInterval(month.value, year.value, (_, p) => !p.isDraft.get.getOrElse(true)).map(a => a.size)

          },
          "only finalized contracts",
        ),
      ),
      div(
        cls := "flex flex-wrap gap-4 items-stretch justify-center",
        MoneyCard(
          "Expenses:",
          Signal.dynamic {
            val sum = jsImplicits.repositories.contracts.existing.value
              .map(p => (p.id -> p.signal.value))
              .filter((_, p) =>
                !p.isDraft.get.getOrElse(true) && ContractPageAttributes().isInInterval(p, month.value, year.value),
              )
              .map((id, contract) => {
                val hourlyWage = ContractPageAttributes()
                  .getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L))
                  .value
                val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
                hoursPerMonth * hourlyWage
              })
              .fold[BigDecimal](0)((a: BigDecimal, b: BigDecimal) => a + b)

            toMoneyString(sum).substring(0, toMoneyString(sum).length() - 2).nn
          },
          "only finalized contracts",
        ),
        MoneyCard(
          "Planned Expenses:",
          Signal.dynamic {
            val sum = jsImplicits.repositories.contracts.existing.value
              .map(p => (p.id -> p.signal.value))
              .filter((_, p) => ContractPageAttributes().isInInterval(p, month.value, year.value))
              .map((id, contract) => {
                val hourlyWage = ContractPageAttributes()
                  .getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L))
                  .value
                val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
                hoursPerMonth * hourlyWage
              })
              .fold[BigDecimal](0)((a: BigDecimal, b: BigDecimal) => a + b)

            toMoneyString(sum).substring(0, toMoneyString(sum).length() - 2).nn
          },
          "all contracts",
        ),
      ),
      Signal.dynamic {
        val projects = jsImplicits.repositories.projects.existing.value.map(p => (p.id -> p.signal.value))
        val hiwis = jsImplicits.repositories.hiwis.all.value.map(p => (p.id -> p.signal.value))
        val supervisors = jsImplicits.repositories.supervisors.all.value.map(p => (p.id -> p.signal.value))

        var contractsPerProject: Map[String, Seq[(String, Contract)]] = Map.empty

        val contracts = jsImplicits.repositories.contracts.existing.value
          .map(p => (p.id -> p.signal.value))
          .filter((_, p) => ContractPageAttributes().isInInterval(p, month.value, year.value))

        projects.foreach((id, _) => {
          contractsPerProject += (id -> contracts
            .filter((_, contract) => contract.contractAssociatedProject.get.getOrElse("") == id))
        })

        val projectsThisMonth = projects
          .filter((id, _) => contractsPerProject.get(id).map(c => c.size > 0).getOrElse(false))

        div(
          cls := "flex flex-col gap-4 md:items-center",
          if (projectsThisMonth.size > 0) {
            projectsThisMonth
              .map((id, project) => {
                TableCard(
                  project.name.get,
                  project.accountName.get,
                  Seq("", "Hiwi", "Supervisor", "From", "To", "€/h", "h/mon", "€/mon"),
                  contractsPerProject(id).map((contractId, contract) => {
                    val moneyPerHour =
                      ContractPageAttributes()
                        .getMoneyPerHour(contractId, contract, contract.contractStartDate.get.getOrElse(0L))
                        .value
                    val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
                    val moneyPerMonth = moneyPerHour * hoursPerMonth
                    val hiwi = hiwis.find((id, _) => id == contract.contractAssociatedHiwi.get.getOrElse(""))
                    val supervisor = supervisors
                      .find((id, _) => id == contract.contractAssociatedSupervisor.get.getOrElse(""))

                    Seq(
                      "",
                      hiwi.map((_, hiwi) => s"${hiwi.firstName.get.getOrElse("")} ${hiwi.firstName.get.getOrElse("")}"),
                      supervisor.map((_, supervisor) => supervisor.name.get.getOrElse("")),
                      toGermanDate(contract.contractStartDate.get.getOrElse(0L)),
                      toGermanDate(contract.contractEndDate.get.getOrElse(0L)),
                      toMoneyString(moneyPerHour),
                      span(hoursPerMonth, " h"),
                      toMoneyString(moneyPerMonth),
                    )
                  }),
                  Seq(
                    "Σ",
                    colSpan := 5,
                    span(
                      contractsPerProject(id)
                        .map((_, contract) => contract.contractHoursPerMonth.get.getOrElse(0))
                        .fold[Int](0)((a, b) => a + b),
                      " h",
                    ),
                    toMoneyString(
                      contractsPerProject(id)
                        .map((id, contract) => {
                          val moneyPerHour =
                            ContractPageAttributes()
                              .getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L))
                              .value
                          val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
                          moneyPerHour * hoursPerMonth
                        })
                        .fold[BigDecimal](0)((a, b) => a + b),
                    ),
                  ),
                )
              })
          } else {
            div(cls := "text-slate-400 dark:text-gray-400", "No projects this month...")
          },
        )
      },
    )
  }
}
