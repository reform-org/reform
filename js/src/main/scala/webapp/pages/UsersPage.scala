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

import UsersPage.*
import webapp.services.Toaster
import webapp.services.RoutingService
import webapp.services.MailService
import webapp.npm.IIndexedDB

import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService
case class UsersPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexeddb: IIndexedDB,
    mailing: MailService,
    webrtc: WebRTCService,
    discovery: DiscoveryService,
) extends EntityPage[User](
      Title("User"),
      None,
      repositories.users,
      repositories.users.all,
      Seq(username, role, comment),
      DefaultEntityRow(),
    ) {}

object UsersPage {
  private def username(using routing: RoutingService) = BuildUIAttribute().string
    .withLabel("Username")
    .require
    .bindAsText[User](
      _.username,
      (u, a) => u.copy(username = a),
    )

  private def role(using routing: RoutingService) = BuildUIAttribute().string
    .withLabel("Role")
    .require
    .bindAsText[User](
      _.role,
      (u, a) => u.copy(role = a),
    )

  private def comment(using routing: RoutingService) = BuildUIAttribute().string
    .withLabel("Comment")
    .withDefaultValue("")
    .bindAsText[User](
      _.comment,
      (u, a) => u.copy(comment = a),
    )
}
