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

import ContractTypesPage.*
import webapp.services.RoutingService
import webapp.npm.IIndexedDB

case class ContractTypesPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[ContractType](
      "Contract types",
      repositories.contractTypes,
      repositories.contractTypes.all,
      Seq(name, files),
      DefaultEntityRow(),
    ) {}

object ContractTypesPage {
  private def name(using routing: RoutingService) = UIAttributeBuilder.string
    .withLabel("Name")
    .require
    .bindAsText[ContractType](
      _.name,
      (s, a) => s.copy(name = a),
    )

  private def files(using
      repositories: Repositories,
      routing: RoutingService,
  ): UIAttribute[ContractType, Seq[String]] =
    UIAttributeBuilder
      .multiSelect(
        repositories.requiredDocuments.all.map(list =>
          list.map(value => value.id -> value.signal.map(_.name.get.getOrElse(""))),
        ),
      )
      .withLabel("Required Documents")
      .require
      .bindAsMultiSelect[ContractType](
        _.files,
        (c, a) => c.copy(files = a),
      )
}
