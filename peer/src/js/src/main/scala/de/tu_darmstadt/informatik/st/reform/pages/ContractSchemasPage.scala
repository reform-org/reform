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
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.entity.*
import org.scalajs.dom.HTMLElement
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

case class ContractSchemasPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[ContractSchema](
      Title("Contract Schema"),
      Some(
        span(
          "Each contract schema has a number of ",
          a(
            cls := "underline cursor-pointer",
            onClick.foreach(e => {
              e.preventDefault()
              e.target.asInstanceOf[HTMLElement].blur()
              jsImplicits.routing.to(DocumentsPage(), true)
            }),
            "documents",
          ),
          " that need to be checked before the contract can be finalized.",
        ),
      ),
      jsImplicits.repositories.contractSchemas,
      jsImplicits.repositories.contractSchemas.all,
      Seq(ContractSchemaAttributes().name, ContractSchemaAttributes().files),
      DefaultEntityRow(),
    ) {}

class ContractSchemaAttributes(using
    jsImplicits: JSImplicits,
) {
  def name: UIAttribute[ContractSchema, String] = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[ContractSchema](
      _.name,
      (s, a) => s.copy(name = a),
    )

  def files: UIAttribute[ContractSchema, Seq[String]] =
    BuildUIAttribute()
      .multiSelect(
        Signal {
          jsImplicits.repositories.requiredDocuments.existing.value.map(value =>
            SelectOption(value.id, value.signal.map(_.name.getOrElse(""))),
          )
        },
      )
      .withCreatePage(DocumentsPage())
      .withLabel("Required Documents")
      .require
      .bindAsMultiSelect[ContractSchema](
        _.files,
        (c, a) => c.copy(files = a),
      )
}
