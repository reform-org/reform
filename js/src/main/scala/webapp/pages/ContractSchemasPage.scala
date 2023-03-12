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

import webapp.services.RoutingService
import webapp.npm.IIndexedDB
import webapp.services.MailService

import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService
case class ContractSchemasPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
    mailing: MailService,
    webrtc: WebRTCService,
    discovery: DiscoveryService,
) extends EntityPage[ContractSchema](
      Title("Contract Schema"),
      Some("Contractschemas Description..."),
      repositories.contractSchemas,
      repositories.contractSchemas.all,
      Seq(ContractSchemaAttributes().name, ContractSchemaAttributes().files),
      DefaultEntityRow(),
    ) {}

class ContractSchemaAttributes(using
    repositories: Repositories,
    routing: RoutingService,
    toaster: Toaster,
    indexeddb: IIndexedDB,
    mailing: MailService,
    webrtc: WebRTCService,
    discovery: DiscoveryService,
) {
  def name = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[ContractSchema](
      _.name,
      (s, a) => s.copy(name = a),
    )

  def files: UIAttribute[ContractSchema, Seq[String]] =
    BuildUIAttribute()
      .multiSelect(
        repositories.requiredDocuments.existing.map(list =>
          list.map(value => SelectOption(value.id, value.signal.map(_.name.get.getOrElse("")))),
        ),
      )
      .withCreatePage(DocumentsPage())
      .withLabel("Required Documents")
      .require
      .bindAsMultiSelect[ContractSchema](
        _.files,
        (c, a) => c.copy(files = a),
      )
}
