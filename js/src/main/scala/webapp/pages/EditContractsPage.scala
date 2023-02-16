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

case class EditContractsPage(contractId: String)(using repositories: Repositories, toaster: Toaster) extends Page {

  /** For now until implemented contract in URL Getter TODO!
    */
  /*
  private val selectedContract: UISelectAttribute[Contract, String] = UISelectAttribute(
    null
    null,
    readConverter = identity,
    writeConverter = identity,
    placeholder = "AssociatedContract",
    options = Repositories.contracts.all.map(list =>
      list.map(value =>
        new UIOption[Signal[String]](
          value.id,
          value.signal.map(v => v._contractAssociatedHiwi.get.getOrElse("") + " " + v._contractAssociatedSupervisor.get.getOrElse("")),
        ),
      ),
    ),
  )
   */
  // Stackoverflow :)

  // def unapply(x: Int): Option[String] =
  //   if (x == 0) Some("Hello, World") else None

  private def contractAssociatedHiwi(using repositories: Repositories): UISelectAttribute[Contract, String] =
    UISelectAttribute(
      _.contractAssociatedHiwi,
      (p, a) => p.copy(contractAssociatedHiwi = a),
      readConverter = identity,
      writeConverter = identity,
      label = "AssociatedHiwi",
      options = repositories.hiwis.all.map(list =>
        list.map(value =>
          new UIOption[Signal[String]](
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
          new UIOption[Signal[String]](
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
          new UIOption[Signal[String]](
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
          new UIOption[Signal[String]](
            value.id,
            value.signal.map(v => v.title.get.getOrElse("")),
          ),
        ),
      ),
      isRequired = true,
    )

  private val currentContract = repositories.contracts.getOrCreate(contractId)
  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode =
    navigationHeader(
      currentContract.map(_ =>
        div(
          div(
            cls := "p-1",
            h1(cls := "text-4xl text-center", "EditContractsPage"),
          ),
          div(
            form(
              br,
              label("CurrentContract:"),
              label(contractId),
              br,
              label("AssociatedHiwi:"),
              contractAssociatedHiwi.renderEdit("", _),
              br,
              label("AssociatedSupervisor:"),
              contractAssociatedSupervisor.renderEdit("", _),
              br,
              label("ContractType:"),
              contractAssociatedType.renderEdit("", _),
              br,
              label("StartDate:"),
              contractStartDate.renderEdit("", _),
              br,
              label("EndDate:"),
              contractEndDate.renderEdit("", _),
              br,
              label("HoursPerMonth:"),
              contractHoursPerMonth.renderEdit("", _),
              br,
              label("AssociatedPaymentLevel:"),
              contractAssociatedPaymentLevel.renderEdit("", _),
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
      ),
    )

}
