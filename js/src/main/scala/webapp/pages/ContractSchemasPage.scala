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
import rescala.default.*
import webapp.components.common.*

import ContractSchemasPage.*

case class ContractSchemasPage()(using repositories: Repositories, toaster: Toaster)
    extends EntityPage[ContractSchema](
      repositories.contractSchemas,
      Seq(name, files),
      DefaultEntityRow(),
    ) {}

object ContractSchemasPage {
  private val name = UIAttributeBuilder.string
    .withLabel("Name")
    .require
    .bindAsText[ContractSchema](
      _.name,
      (s, a) => s.copy(name = a),
    )

  private def files(using repositories: Repositories): UIMultiSelectAttribute[ContractSchema, Seq[String]] =
    UIMultiSelectAttribute(
      _.files,
      (p, a) => p.copy(files = a),
      readConverter = r => r.mkString(", "),
      writeConverter = w => w.split(", ").toSeq,
      label = "Required Documents",
      options = repositories.requiredDocuments.all.map(list =>
        list.map(value => new SelectOption[Signal[String]](value.id, value.signal.map(v => v.name.get.getOrElse("")))),
      ),
      isRequired = true,
    )
}
