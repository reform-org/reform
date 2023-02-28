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

case class SupervisorsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[Supervisor](
      "Supervisors",
      repositories.supervisors,
      repositories.supervisors.all,
      Seq(firstName, lastName, eMail),
      DefaultEntityRow(),
    ) {}

object SupervisorsPage {
  private def firstName(using routing: RoutingService) = UIAttributeBuilder.string
    .withLabel("First Name")
    .require
    .bindAsText[Supervisor](
      _.firstName,
      (s, a) => s.copy(firstName = a),
    )

  private def lastName(using routing: RoutingService) = UIAttributeBuilder.string
    .withLabel("Last Name")
    .require
    .bindAsText[Supervisor](
      _.lastName,
      (s, a) => s.copy(lastName = a),
    )

  private def eMail(using routing: RoutingService) = UIAttributeBuilder.string
    .withLabel("Email")
    .require
    .bindAsText[Supervisor](
      _.eMail,
      (s, a) => s.copy(eMail = a),
    )
}
