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

import DocumentsPage.*

import webapp.entity.Document
import webapp.services.RoutingService
import webapp.npm.IIndexedDB
import webapp.services.MailService

import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService
import webapp.JSImplicits

import outwatch.*
import outwatch.dsl.*
import org.scalajs.dom.HTMLElement

case class DocumentsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Document](
      Title("Document"),
      Some(
        span(
          "Each ",
          a(
            cls := "underline cursor-pointer",
            onClick.foreach(e => {
              e.preventDefault()
              e.target.asInstanceOf[HTMLElement].blur()
              jsImplicits.routing.to(SalaryChangesPage(), true)
            }),
            "contract schema",
          ),
          " has a number of documents that need to be checked before the contract can be finalized.",
        ),
      ),
      jsImplicits.repositories.requiredDocuments,
      jsImplicits.repositories.requiredDocuments.all,
      Seq(DocumentAttributes().name),
      DefaultEntityRow(),
    ) {}

class DocumentAttributes(using jsImplicits: JSImplicits) {
  def name = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[Document](
      _.name,
      (d, a) => d.copy(name = a),
    )
}