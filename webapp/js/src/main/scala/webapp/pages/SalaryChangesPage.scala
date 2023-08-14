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
import webapp.services.MailService

import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService
import webapp.JSImplicits

import outwatch.*
import outwatch.dsl.*
import org.scalajs.dom.HTMLElement

case class SalaryChangesPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[SalaryChange](
      Title("Salary Change"),
      Some(
        span(
          "A salary change defines the amount of money a hiwi is getting point from one date until the next salary change is taking effect. The limit is the Minijob limit for this pay at the specified time. If there is no salary change for a payment level the payment level value defaults to ",
          i("0.00 €"),
          ". Payment levels can be created ",
          a(
            cls := "underline cursor-pointer",
            onClick.foreach(e => {
              e.preventDefault()
              e.target.asInstanceOf[HTMLElement].blur()
              jsImplicits.routing.to(PaymentLevelsPage(), true)
            }),
            "here",
          ),
          ".",
        ),
      ),
      jsImplicits.repositories.salaryChanges,
      jsImplicits.repositories.salaryChanges.all,
      Seq(
        SalaryChangeAttributes().salaryChangeValue,
        SalaryChangeAttributes().salaryChangeLimit,
        SalaryChangeAttributes().salaryChangePaymentLevel,
        SalaryChangeAttributes().salaryChangeFromDate,
      ),
      DefaultEntityRow(),
    ) {}

class SalaryChangeAttributes(using
    jsImplicits: JSImplicits,
) {
  def salaryChangeValue = BuildUIAttribute().money
    .withLabel("Value")
    .withMin("0")
    .withRegex("[0-9]+([\\.,][0-9]+)?")
    .require
    .bindAsNumber[SalaryChange](
      _.value,
      (s, a) => s.copy(value = a),
    )

  def salaryChangeLimit = BuildUIAttribute().money
    .withLabel("Limit")
    .withMin("0")
    .withRegex("[0-9]+([\\.,][0-9]+)?")
    .require
    .bindAsNumber[SalaryChange](
      _.limit,
      (s, a) => s.copy(limit = a),
    )

  def salaryChangePaymentLevel: UIAttribute[SalaryChange, String] = {
    BuildUIAttribute()
      .select(
        Signal {
          jsImplicits.repositories.paymentLevels.existing.value.map(value =>
            SelectOption(value.id, value.signal.map(_.identifier.get.getOrElse(""))),
          )
        },
      )
      .withCreatePage(PaymentLevelsPage())
      .withLabel("Payment Level")
      .require
      .bindAsSelect(
        _.paymentLevel,
        (p, a) => p.copy(paymentLevel = a),
      )
  }

  def salaryChangeFromDate = BuildUIAttribute().date
    .withLabel("From")
    .require
    .bindAsDatePicker[SalaryChange](
      _.fromDate,
      (s, a) => s.copy(fromDate = a),
    )
}