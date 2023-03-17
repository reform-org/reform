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
import webapp.services.Toaster

import PaymentLevelsPage.*
import webapp.services.RoutingService
import webapp.npm.IIndexedDB
import rescala.default.*
import webapp.npm.JSUtils.toMoneyString
import webapp.services.MailService
import webapp.JSImplicits

import outwatch.*
import outwatch.dsl.*

import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService
import org.scalajs.dom.HTMLElement
case class PaymentLevelsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[PaymentLevel](
      Title("Payment Level"),
      Some(
        span(
          "To check the correct checkbox in the PDF Contract you need to provide a payment level per contract which corresponds with the name of the checkbox in the PDF form. To find out the names of the PDF form fields you can use the PDF Inspector on the ",
          a(
            cls := "underline cursor-pointer",
            onClick.foreach(e => {
              e.preventDefault()
              e.target.asInstanceOf[HTMLElement].blur()
              jsImplicits.routing.to(SettingsPage(), true)
            }),
            "settings page",
          ),
          ". Each paymentlevel has a value that is used for cost calculations, those can be set on the ",
          a(
            cls := "underline cursor-pointer",
            onClick.foreach(e => {
              e.preventDefault()
              e.target.asInstanceOf[HTMLElement].blur()
              jsImplicits.routing.to(SalaryChangesPage(), true)
            }),
            "salarychanges page",
          ),
          ".",
        ),
      ),
      jsImplicits.repositories.paymentLevels,
      jsImplicits.repositories.paymentLevels.all,
      Seq(
        PaymentLevelAttributes().title,
        PaymentLevelAttributes().pdfCheckboxName,
        PaymentLevelAttributes().currentValue,
      ),
      DefaultEntityRow(),
    ) {}

class PaymentLevelAttributes(using jsImplicits: JSImplicits) {
  def title = BuildUIAttribute().string
    .withLabel("Title")
    .require
    .bindAsText[PaymentLevel](
      _.title,
      (p, a) => p.copy(title = a),
    )

  def pdfCheckboxName = BuildUIAttribute().string
    .withLabel("PDF Checkbox Name")
    .require
    .bindAsText[PaymentLevel](
      _.pdfCheckboxName,
      (p, a) => p.copy(pdfCheckboxName = a),
    )

  def currentValue =
    new UIReadOnlyAttribute[PaymentLevel, String](
      label = "Current Value",
      getter = (id, paymentLevel) =>
        Signal.dynamic {
          val salaryChanges = jsImplicits.repositories.salaryChanges.all.value
          toMoneyString(
            salaryChanges
              .map(_.signal.value)
              .filter(_.paymentLevel.get.getOrElse("") == id)
              .sortWith(_.fromDate.get.getOrElse(0L) > _.fromDate.get.getOrElse(0L))
              .headOption
              .flatMap(_.value.get)
              .getOrElse(0),
          )
        },
      readConverter = identity,
    )
}
