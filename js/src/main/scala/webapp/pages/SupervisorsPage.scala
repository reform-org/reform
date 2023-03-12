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

import SupervisorsPage.*
import webapp.services.RoutingService
import webapp.npm.IIndexedDB
import webapp.services.MailService

import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService
case class SupervisorsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
    mailing: MailService,
    webrtc: WebRTCService,
    discovery: DiscoveryService,
) extends EntityPage[Supervisor](
      Title("Supervisor"),
      None,
      repositories.supervisors,
      repositories.supervisors.all,
      Seq(name, eMail),
      DefaultEntityRow(),
    ) {}

object SupervisorsPage {
  private def name(using routing: RoutingService) = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[Supervisor](
      _.name,
      (s, a) => s.copy(name = a),
    )

  private def eMail(using routing: RoutingService) = BuildUIAttribute().email
    .withLabel("Email")
    .require
    .bindAsText[Supervisor](
      _.eMail,
      (s, a) => s.copy(eMail = a),
    )
}
