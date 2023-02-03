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

import webapp.Repositories
import webapp.entity.*
import rescala.default.*
import webapp.utils.Date


private val contractAssociatedHiwi: UISelectAttribute[Contract, String] = UISelectAttribute(
  _._contractAssociatedHiwi,
  (p, a) => p.copy(_contractAssociatedHiwi = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "AssociatedHiwi",
  options = Repositories.hiwis.all.map(list =>
    list.map(value => new UIOption[Signal[String]](value.id, value.signal.map(v => v._firstName.get.getOrElse("")))),
  ),

)

private val contractType: UIAttribute[Contract, String] = UIAttribute(
  _._contractType,
  (p, a) => p.copy(_contractType = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "ContractType",
)

private val contractAssociatedSupervisor: UIAttribute[Contract, String] = UIAttribute(
  _._contractAssociatedSupervisor,
  (p, a) => p.copy(_contractAssociatedSupervisor = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "AssociatedSupervisor",
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

private val contractAssociatedPaymentLevel: UIAttribute[Contract, String] = UIAttribute(
  _._contractAssociatedPaymentLevel,
  (p, a) => p.copy(_contractAssociatedPaymentLevel = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "AssociatedPaymentLevel",
)

case class ContractsPage()
    extends EntityPage[Contract](
      Repositories.contracts,
      Seq(
        contractAssociatedHiwi,
        contractType,
        contractAssociatedSupervisor,
        contractStartDate,
        contractEndDate,
        contractHoursPerMonth,
        contractAssociatedPaymentLevel,
      ),
    ) {}
