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
import webapp.utils.Seqnal.*
import webapp.npm.JSUtils.toMoneyString

case class PaymentLevelsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[PaymentLevel](
      "Payment Levels",
      repositories.paymentLevels,
      repositories.paymentLevels.all,
      Seq(title, pdfCheckboxName, currentValue),
      DefaultEntityRow(),
    ) {}

object PaymentLevelsPage {
  private def title(using routing: RoutingService) = UIAttributeBuilder.string
    .withLabel("Title")
    .require
    .bindAsText[PaymentLevel](
      _.title,
      (p, a) => p.copy(title = a),
    )

  private def pdfCheckboxName(using routing: RoutingService) = UIAttributeBuilder.string
    .withLabel("PDF Checkbox Name")
    .require
    .bindAsText[PaymentLevel](
      _.pdfCheckboxName,
      (p, a) => p.copy(pdfCheckboxName = a),
    )

  private def currentValue(using repositories: Repositories) =
    new UIReadOnlyAttribute[PaymentLevel, String](
      label = "Current Value",
      getter = (id, paymentLevel) =>
        Signal {
          val salaryChanges = repositories.salaryChanges.all.value
          Signal {
            toMoneyString(
              Signal(
                salaryChanges
                  .map(a => Signal { a.signal.value }),
              ).flatten.value
                .filter(_.paymentLevel.get.getOrElse("") == id)
                .sortWith(_.fromDate.get.getOrElse(0L) > _.fromDate.get.getOrElse(0L))
                .headOption match {
                case None     => 0
                case Some(sc) => sc.value.get.getOrElse(0)
              },
            )
          }
        }.flatten,
      readConverter = identity,
    )
}
