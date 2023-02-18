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
import rescala.default.*
import webapp.components.navigationHeader
import webapp.services.Page
import webapp.entity.*
import webapp.{*, given}
import webapp.services.DiscoveryService
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.services.Toaster
import webapp.repo.Synced
import webapp.components.common.*

case class EditContractsPage(contractId: String)(using repositories: Repositories, toaster: Toaster) extends Page {

  private val currentContract = repositories.contracts.getOrCreate(contractId)

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    div(currentContract.map(currentContract => {
      InnerEditContractsPage(currentContract).render()
    }))
  }
}

case class InnerEditContractsPage(contract: Synced[Contract]) {

  private def contractAssociatedHiwi(using repositories: Repositories): UISelectAttribute[Contract, String] =
    UISelectAttribute(
      _.contractAssociatedHiwi,
      (p, a) => p.copy(contractAssociatedHiwi = a),
      readConverter = identity,
      writeConverter = identity,
      label = "AssociatedHiwi",
      options = repositories.hiwis.all.map(list =>
        list.map(value =>
          new SelectOption[Signal[String]](
            value.id,
            value.signal.map(v => v.firstName.get.getOrElse("") + " " + v.lastName.get.getOrElse("")),
          ),
        ),
      ),
      isRequired = true,
    )

  private def contractAssociatedSupervisor(using repositories: Repositories): UISelectAttribute[Contract, String] =
    UISelectAttribute(
      _.contractAssociatedSupervisor,
      (p, a) => p.copy(contractAssociatedSupervisor = a),
      readConverter = identity,
      writeConverter = identity,
      label = "AssociatedSupervisors",
      options = repositories.supervisors.all.map(list =>
        list.map(value =>
          new SelectOption[Signal[String]](
            value.id,
            value.signal.map(v => v.firstName.get.getOrElse("") + " " + v.lastName.get.getOrElse("")),
          ),
        ),
      ),
      isRequired = true,
    )

  private def contractAssociatedType(using repositories: Repositories): UISelectAttribute[Contract, String] =
    UISelectAttribute(
      _.contractType,
      (p, a) => p.copy(contractType = a),
      readConverter = identity,
      writeConverter = identity,
      label = "ContractType",
      options = repositories.contractSchemas.all.map(list =>
        list.map(value =>
          new SelectOption[Signal[String]](
            value.id,
            value.signal.map(v => v.name.get.getOrElse("")),
          ),
        ),
      ),
      isRequired = true,
    )

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

  private def contractAssociatedPaymentLevel(using repositories: Repositories): UISelectAttribute[Contract, String] =
    UISelectAttribute(
      _.contractType,
      (p, a) => p.copy(contractType = a),
      readConverter = identity,
      writeConverter = identity,
      label = "AssociatedPaymentLevel",
      options = repositories.paymentLevels.all.map(list =>
        list.map(value =>
          new SelectOption[Signal[String]](
            value.id,
            value.signal.map(v => v.title.get.getOrElse("")),
          ),
        ),
      ),
      isRequired = true,
    )

  var currentContract = Var(Option(contract.signal.now))

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
          h1(cls := "text-4xl text-center", "EditContractsPage"),
        ),
        div(
          form(
            br,
            label("CurrentContract:"),
            label(contract.id),
            br,
            label("AssociatedHiwi:"),
            contractAssociatedHiwi.renderEdit("", currentContract),
            br,
            label("AssociatedSupervisor:"),
            contractAssociatedSupervisor.renderEdit("", currentContract),
            br,
            label("ContractType:"),
            contractAssociatedType.renderEdit("", currentContract),
            br,
            label("StartDate:"),
            contractStartDate.renderEdit("", currentContract),
            br,
            label("EndDate:"),
            contractEndDate.renderEdit("", currentContract),
            br,
            label("HoursPerMonth:"),
            contractHoursPerMonth.renderEdit("", currentContract),
            br,
            label("AssociatedPaymentLevel:"),
            contractAssociatedPaymentLevel.renderEdit("", currentContract),
            button(
              cls := "btn",
              idAttr := "confirmEdit",
              "Save",
              // onClick.foreach(_ => cancelEdit()), TODO implement confirmEdit
            ),
            button(
              cls := "btn",
              idAttr := "cancelEdit",
              "Cancel",
              // onClick.foreach(_ => cancelEdit()), TODO implement cancelEdit
            ),
          ),
        ),
      ),
    )

}
