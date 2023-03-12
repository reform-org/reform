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

import org.scalajs.dom.*
import scala.scalajs.js
import outwatch.*
import outwatch.dsl.*
import webapp.{*, given}
import webapp.components.navigationHeader
import webapp.npm.*
import webapp.services.DiscoveryService
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService

import webapp.components.common.*

import webapp.services.{ToastMode, ToastType, Toaster}
import webapp.given_ExecutionContext
import webapp.components.{Modal, ModalButton}
import webapp.utils.Futures.*
import webapp.utils.{exportIndexedDBJson, importIndexedDBJson}
import webapp.npm.JSUtils.downloadFile
import org.scalajs.dom.HTMLInputElement
import rescala.default.*
import webapp.components.*

import scala.util.Success
import scala.util.Failure
import webapp.npm.JSUtils.toHumanMonth
import webapp.pages.ContractsPage.getSalaryChange
import webapp.npm.JSUtils.toMilliseconds
import webapp.pages.ContractsPage.getMoneyPerHour
import webapp.npm.JSUtils.toMoneyString
import webapp.entity.Contract
import webapp.npm.JSUtils.toGermanDate

case class HomePage()(using indexeddb: IIndexedDB) extends Page {

  def getContractsForInterval(month: Int, year: Int, pred: (String, Contract) => Boolean = (a, b) => true)(using
      repositories: Repositories,
  ) = {
    Signal.dynamic {
      repositories.contracts.existing.value
        .map(p => (p.id -> p.signal.value))
        .filter((id, p) => pred(id, p) && p.isInInterval(month, year))
    }
  }

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    val year =
      routing.getQueryParameterAsString("year").map(p => if (p == "") new js.Date().getFullYear().toInt else p.toInt)
    val month =
      routing
        .getQueryParameterAsString("month")
        .map(p => if (p == "") new js.Date().getMonth().toInt else p.toInt)

    navigationHeader(
      div(
        cls := "flex flex-col gap-2 max-w-sm",
        div(
          cls := "flex flex-row gap-2 items-center",
          IconButton(
            ButtonStyle.LightDefault,
            icons.Previous(cls := "w-6 h-6"),
            onClick.foreach(_ => {
              val m = month.now
              val y = year.now
              routing.updateQueryParameters(
                Map(
                  ("month" -> (if (m == 1) "12" else (m - 1).toString)),
                  ("year" -> (if (m == 1) (y - 1).toString else y.toString)),
                ),
              )
            }),
          ),
          div(Signal { toHumanMonth(month.value) }, " ", year),
          IconButton(
            ButtonStyle.LightDefault,
            icons.Next(cls := "w-6 h-6"),
            onClick.foreach(_ => {
              val m = month.now
              val y = year.now
              routing.updateQueryParameters(
                Map(
                  ("month" -> (if (m == 12) "1" else (m + 1).toString)),
                  ("year" -> (if (m == 12) (y + 1).toString else y.toString)),
                ),
              )
            }),
          ),
        ),
        "Total Contracts:",
        Signal.dynamic {
          getContractsForInterval(month.value, year.value).map(a => a.size)
        },
        "Draft Contracts:",
        Signal.dynamic {
          getContractsForInterval(month.value, year.value, (_, p) => p.isDraft.get.getOrElse(true)).map(a => a.size)
        },
        "Actual Contracts:",
        Signal.dynamic {
          getContractsForInterval(month.value, year.value, (_, p) => !p.isDraft.get.getOrElse(true)).map(a => a.size)

        },
        "Total Expenses:",
        Signal.dynamic {
          val sum = repositories.contracts.existing.value
            .map(p => (p.id -> p.signal.value))
            .filter((id, p) => !p.isDraft.get.getOrElse(true) && p.isInInterval(month.value, year.value))
            .map((id, contract) => {
              val hourlyWage = getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L)).value
              val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
              hoursPerMonth * hourlyWage
            })
            .fold[BigDecimal](0)((a: BigDecimal, b: BigDecimal) => a + b)

          toMoneyString(sum)
        },
        "Total Expenses with drafts:",
        Signal.dynamic {
          val sum = repositories.contracts.existing.value
            .map(p => (p.id -> p.signal.value))
            .filter((id, p) => p.isInInterval(month.value, year.value))
            .map((id, contract) => {
              val hourlyWage = getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L)).value
              val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
              hoursPerMonth * hourlyWage
            })
            .fold[BigDecimal](0)((a: BigDecimal, b: BigDecimal) => a + b)

          toMoneyString(sum)
        },
        Signal.dynamic {
          val projects = repositories.projects.existing.value.map(p => (p.id -> p.signal.value))
          val hiwis = repositories.hiwis.all.value.map(p => (p.id -> p.signal.value))
          val supervisors = repositories.supervisors.all.value.map(p => (p.id -> p.signal.value))

          var contractsPerProject: Map[String, Seq[(String, Contract)]] = Map.empty

          val contracts = repositories.contracts.existing.value
            .map(p => (p.id -> p.signal.value))
            .filter((id, p) => p.isInInterval(month.value, year.value))

          projects.foreach((id, project) => {
            contractsPerProject += (id -> contracts
              .filter((_, contract) => contract.contractAssociatedProject.get.getOrElse("") == id))
          })

          projects
            .filter((id, _) => contractsPerProject.get(id).map(c => c.size > 0).getOrElse(false))
            .map((id, project) => {
              div(
                project.name.get,
                project.accountName.get,
                table(
                  thead(
                    tr(
                      th(),
                      th("Hiwi"),
                      th("Supervisor"),
                      th("From"),
                      th("To"),
                      th("€/h"),
                      th("h/mon"),
                      th("€/mon"),
                    ),
                  ),
                  tbody(
                    contractsPerProject(id).map((contractId, contract) => {
                      val moneyPerHour =
                        getMoneyPerHour(contractId, contract, contract.contractStartDate.get.getOrElse(0L)).value
                      val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
                      val moneyPerMonth = moneyPerHour * hoursPerMonth
                      val hiwi = hiwis.find((id, hiwi) => id == contract.contractAssociatedHiwi.get.getOrElse(""))
                      val supervisor = supervisors
                        .find((id, supervisor) => id == contract.contractAssociatedSupervisor.get.getOrElse(""))
                      tr(
                        td(),
                        td(
                          hiwi.map((_, hiwi) =>
                            s"${hiwi.firstName.get.getOrElse("")} ${hiwi.firstName.get.getOrElse("")}",
                          ),
                        ),
                        td(supervisor.map((_, supervisor) => supervisor.name.get.getOrElse(""))),
                        td(toGermanDate(contract.contractStartDate.get.getOrElse(0L))),
                        td(toGermanDate(contract.contractEndDate.get.getOrElse(0L))),
                        td(toMoneyString(moneyPerHour)),
                        td(hoursPerMonth, " h"),
                        td(toMoneyString(moneyPerMonth)),
                      )
                    }),
                  ),
                  tfoot(
                    tr(
                      td("Σ"),
                      td(colSpan := 5),
                      td(
                        contractsPerProject(id)
                          .map((_, contract) => contract.contractHoursPerMonth.get.getOrElse(0))
                          .fold[Int](0)((a, b) => a + b),
                        " h",
                      ),
                      td(
                        toMoneyString(
                          contractsPerProject(id)
                            .map((id, contract) => {
                              val moneyPerHour =
                                getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L)).value
                              val hoursPerMonth = contract.contractHoursPerMonth.get.getOrElse(0)
                              moneyPerHour * hoursPerMonth
                            })
                            .fold[BigDecimal](0)((a, b) => a + b),
                        ),
                      ),
                    ),
                  ),
                ),
              )
            })
        },
      ),
    )
  }
}
