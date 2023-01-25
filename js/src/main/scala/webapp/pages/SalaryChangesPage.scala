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

private val salaryChangeValue: UIAttribute[SalaryChange, Int] = UIAttribute(
  _._value,
  (p, a) => p.copy(_value = a),
  readConverter = number => (number / 100.0).toString,
  writeConverter = number => Math.round(number.toFloat * 100),
  placeholder = "Value",
  fieldType = "number",
)

private val salaryChangePaymentLevel: UISelectAttribute[SalaryChange, String] = UISelectAttribute(
  _._paymentLevel,
  (p, a) => p.copy(_paymentLevel = a),
  // readConverter = str => Repositories.paymentLevels.all.map(list => list.filter(paymentLevel => paymentLevel.id == str).map(value => value.signal.map(v => v._title.get.getOrElse("")))),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "PaymentLevel",
  options = Repositories.paymentLevels.all.map(list =>
    list.map(value => new UIOption[Signal[String]](value.id, value.signal.map(v => v.title.get.getOrElse("")))),
  ),
)

private val salaryChangeFromDate: UIDateAttribute[SalaryChange, Long] = UIDateAttribute(
  _._fromDate,
  (p, a) => p.copy(_fromDate = a),
  readConverter = Date.epochDayToDate(_, "dd.MM.yyyy"),
  editConverter = Date.epochDayToDate(_, "yyyy-MM-dd"),
  writeConverter = Date.dateToEpochDay(_, "yyyy-MM-dd"),
  placeholder = "From",
  // min = "2023-01-24",
)

case class SalaryChangesPage()
    extends EntityPage[SalaryChange](
      Repositories.salaryChanges,
      Seq(salaryChangeValue, salaryChangePaymentLevel, salaryChangeFromDate),
    ) {}
