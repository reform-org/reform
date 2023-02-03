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

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.components.navigationHeader
import webapp.services.Page
import webapp.entity.*
import webapp.utils.Date
import webapp.{*, given}

case class EditContractsPage() extends Page {

  /**
   * For now until implemented contract in URL Getter TODO!
   */
  private val selectedContract: UISelectAttribute[Contract, String] = UISelectAttribute(
    null,
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

  private val contractAssociatedHiwi: UISelectAttribute[Contract, String] = UISelectAttribute(
    _._contractAssociatedHiwi,
    (p, a) => p.copy(_contractAssociatedHiwi = a),
    readConverter = identity,
    writeConverter = identity,
    placeholder = "AssociatedHiwi",
    options = Repositories.hiwis.all.map(list =>
      list.map(value =>
        new UIOption[Signal[String]](
          value.id,
          value.signal.map(v => v._firstName.get.getOrElse("") + " " + v._lastName.get.getOrElse("")),
        ),
      ),
    ),
  )

  private val contractAssociatedSupervisor: UISelectAttribute[Contract, String] = UISelectAttribute(
    _._contractAssociatedSupervisor,
    (p, a) => p.copy(_contractAssociatedSupervisor = a),
    readConverter = identity,
    writeConverter = identity,
    placeholder = "AssociatedSupervisors",
    options = Repositories.supervisor.all.map(list =>
      list.map(value =>
        new UIOption[Signal[String]](
          value.id,
          value.signal.map(v => v._firstName.get.getOrElse("") + " " + v._lastName.get.getOrElse("")),
        ),
      ),
    ),
  )

  private val contractAssociatedType: UISelectAttribute[Contract, String] = UISelectAttribute(
    _._contractType,
    (p, a) => p.copy(_contractType = a),
    readConverter = identity,
    writeConverter = identity,
    placeholder = "ContractType",
    options = Repositories.contractSchemas.all.map(list =>
      list.map(value =>
        new UIOption[Signal[String]](
          value.id,
          value.signal.map(v => v._name.get.getOrElse("")),
        ),
      ),
    ),
  )

  private val contractStartDate: UIDateAttribute[Contract, Long] = UIDateAttribute(
    _._contractStartDate,
    (p, a) => p.copy(_contractStartDate = a),
    readConverter = Date.epochDayToDate(_, "dd.MM.yyyy"),
    editConverter = Date.epochDayToDate(_, "yyyy-MM-dd"),
    writeConverter = Date.dateToEpochDay(_, "yyyy-MM-dd"),
    placeholder = "StartDate",
  )

  private val contractEndDate: UIDateAttribute[Contract, Long] = UIDateAttribute(
    _._contractEndDate,
    (p, a) => p.copy(_contractEndDate = a),
    readConverter = Date.epochDayToDate(_, "dd.MM.yyyy"),
    editConverter = Date.epochDayToDate(_, "yyyy-MM-dd"),
    writeConverter = Date.dateToEpochDay(_, "yyyy-MM-dd"),
    placeholder = "EndDate",
  )

  private val contractHoursPerMonth: UIAttribute[Contract, Int] = UIAttribute(
    _._contractHoursPerMonth,
    (p, a) => p.copy(_contractHoursPerMonth = a),
    readConverter = _.toString(),
    writeConverter = _.toInt,
    placeholder = "HoursPerMonth",
    fieldType = "number",
  )
  /*
  private val contractAssociatedPaymentLevel: UIAttribute[Contract, String] = UIAttribute(
    _._contractAssociatedPaymentLevel,
    (p, a) => p.copy(_contractAssociatedPaymentLevel = a),
    readConverter = identity,
    writeConverter = identity,
    placeholder = "AssociatedPaymentLevel",
  )
   */
  private val contractAssociatedPaymentLevel: UISelectAttribute[Contract, String] = UISelectAttribute(
    _._contractAssociatedPaymentLevel,
    (p, a) => p.copy(_contractAssociatedPaymentLevel = a),
    readConverter = identity,
    writeConverter = identity,
    placeholder = "AssociatedPaymentLevel",
    options = Repositories.paymentLevels.all.map(list =>
      list.map(value =>
        new UIOption[Signal[String]](
          value.id,
          value.signal.map(v => v._title.get.getOrElse("")),
        ),
      ),
    ),
  )

  private val currentContract = Var(Option(Contract.empty.copy(_contractAssociatedHiwi = Attribute.empty.set("1"))))

  def render(using services: Services): VNode =
    div(
      navigationHeader,
      div(
        cls := "p-1",
        h1(cls := "text-4xl text-center", "EditContractsPage"),
      ),
      div(
        // button(tpe := "button", cls := "btn btn-default", tabIndex := -1, "Button"),
        form(
          //label("Contract:"),
          //selectedContract.renderEdit(currentContract),
          br,
          label("AssociatedHiwi:"),
          contractAssociatedHiwi.renderEdit(currentContract),
          br,
          label("AssociatedSupervisor:"),
          contractAssociatedSupervisor.renderEdit(currentContract),
          br,
          label("ContractType:"),
          contractAssociatedType.renderEdit(currentContract),
          br,
          label("StartDate:"),
          contractStartDate.renderEdit(currentContract),
          br,
          label("EndDate:"),
          contractEndDate.renderEdit(currentContract),
          br,
          label("HoursPerMonth:"),
          contractHoursPerMonth.renderEdit(currentContract),
          br,
          label("AssociatedPaymentLevel:"),
          contractAssociatedPaymentLevel.renderEdit(currentContract),
          br,
          button(
            cls := "btn",
            idAttr := "confirmEdit",
            "Edit",
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
    )

}
