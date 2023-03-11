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
import webapp.services.Toaster
import webapp.components.common.*
import webapp.npm.IIndexedDB

import SalaryChangesPage.*
import webapp.services.RoutingService

case class SalaryChangesPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexeddb: IIndexedDB,
) extends EntityPage[SalaryChange](
      Title("Salary Change"),
      Some("Salary Changes Description..."),
      repositories.salaryChanges,
      repositories.salaryChanges.all,
      Seq(salaryChangeValue, salaryChangeLimit, salaryChangePaymentLevel, salaryChangeFromDate),
      DefaultEntityRow(),
    ) {}

object SalaryChangesPage {
  private def salaryChangeValue(using routing: RoutingService) = UIAttributeBuilder.money
    .withLabel("Value")
    .withMin("0")
    .require
    .bindAsNumber[SalaryChange](
      _.value,
      (s, a) => s.copy(value = a),
    )

  private def salaryChangeLimit(using routing: RoutingService) = UIAttributeBuilder.money
    .withLabel("Limit")
    .withMin("0")
    .require
    .bindAsNumber[SalaryChange](
      _.limit,
      (s, a) => s.copy(limit = a),
    )

  private def salaryChangePaymentLevel(using
      repositories: Repositories,
      routing: RoutingService,
      toaster: Toaster,
      indexeddb: IIndexedDB,
  ): UIAttribute[SalaryChange, String] = {
    UIAttributeBuilder
      .select(
        repositories.paymentLevels.all.map(list =>
          list.map(value => SelectOption(value.id, value.signal.map(v => v.identifier.get.getOrElse("")))),
        ),
      )
      .withCreatePage(PaymentLevelsPage())
      .withLabel("Payment Level")
      .require
      .bindAsSelect(
        _.paymentLevel,
        (p, a) => p.copy(paymentLevel = a),
      )
  }

  private def salaryChangeFromDate(using RoutingService) = UIAttributeBuilder.date
    .withLabel("From")
    .require
    .bindAsDatePicker[SalaryChange](
      _.fromDate,
      (s, a) => s.copy(fromDate = a),
    )
}
