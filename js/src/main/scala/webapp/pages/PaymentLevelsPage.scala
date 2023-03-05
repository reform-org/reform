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

case class PaymentLevelsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[PaymentLevel](
      Title("Payment Level"),
      Some("Paymentlevel Description..."),
      repositories.paymentLevels,
      repositories.paymentLevels.all,
      Seq(title, pdfCheckboxName),
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
}
