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

import rescala.default.*
import webapp.Repositories
import webapp.entity.*
import SalaryChangesPage.*

case class SalaryChangesPage()(using repositories: Repositories)
    extends EntityPage[SalaryChange](
      repositories.salaryChanges,
      Seq(salaryChangeValue, salaryChangePaymentLevel, salaryChangeFromDate),
    ) {}

object SalaryChangesPage {
  private val salaryChangeValue = UIAttributeBuilder.float
    .withLabel("Value")
    .require
    .map[Int](_ / 100.0f, f => Math.round(f * 100.0f))
    .bind[SalaryChange](
      _.value,
      (s, a) => s.copy(value = a),
    )

  private def salaryChangePaymentLevel(using repositories: Repositories): UISelectAttribute[SalaryChange, String] =
    UISelectAttribute(
      _.paymentLevel,
      (p, a) => p.copy(paymentLevel = a),
      // readConverter = str => Repositories.paymentLevels.all.map(list => list.filter(paymentLevel => paymentLevel.id == str).map(value => value.signal.map(v => v._title.get.getOrElse("")))),
      readConverter = identity,
      writeConverter = identity,
      label = "PaymentLevel",
      options = repositories.paymentLevels.all.map(list =>
        list.map(value => new UIOption[Signal[String]](value.id, value.signal.map(v => v.title.get.getOrElse("")))),
      ),
      isRequired = true,
    )

  private val salaryChangeFromDate = UIAttributeBuilder.date
    .withLabel("From")
    .require
    .bind[SalaryChange](
      _.fromDate,
      (s, a) => s.copy(fromDate = a),
    )
}
