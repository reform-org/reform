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
package de.tu_darmstadt.informatik.st.reform.pages

import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.entity.Document
import de.tu_darmstadt.informatik.st.reform.entity.*
import org.scalajs.dom.HTMLElement
import outwatch.*
import outwatch.dsl.*

case class DocumentsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Document](
      Title("Document"),
      span(
        "Each ",
        a(
          cls := "underline cursor-pointer",
          onClick.foreach(e => {
            e.preventDefault()
            e.target.asInstanceOf[HTMLElement].blur()
            jsImplicits.routing.to(ContractSchemasPage(), true)
          }),
          "contract schema",
        ),
        " has a number of documents that need to be checked before the contract can be finalized.",
      ),
      jsImplicits.repositories.documents,
      jsImplicits.repositories.documents.all,
      Seq(
        DocumentAttributes().name,
        DocumentAttributes().location,
        DocumentAttributes().autofill,
        DocumentAttributes().mailto,
      ),
      DefaultEntityRow(),
    ) {}

class DocumentAttributes(using jsImplicits: JSImplicits) {
  def name: UIAttribute[Document, String] = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[Document](
      _.name,
      (d, a) => d.copy(name = a),
    )

  def location: UIAttribute[Document, String] = BuildUIAttribute().string
    .withLabel("Location")
    .require
    .bindAsText[Document](
      _.location,
      (d, a) => d.copy(location = a),
    )

  def autofill: UIAttribute[Document, Autofill] = BuildUIAttribute()
    .enumSelect(Autofill.values, Autofill.valueOf)
    .withLabel("Automatic filling")
    .bindAsSelect[Document](
      _.autofill,
      (d, a) => d.copy(autofill = a),
    )

  def mailto: UIAttribute[Document, DocumentForWhom] = BuildUIAttribute()
    .enumSelect(DocumentForWhom.values, DocumentForWhom.valueOf)
    .withLabel("For Whom?")
    .bindAsSelect[Document](
      _.mailto,
      (d, a) => d.copy(mailto = a),
    )
}
