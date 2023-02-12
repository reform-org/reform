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

case class SupervisorsPage()(using repositories: Repositories, toaster: Toaster)
    extends EntityPage[Supervisor](repositories.supervisors, Seq(firstName, lastName, eMail)) {}

object SupervisorsPage {
  private val firstName = UIAttributeBuilder.string
    .withLabel("First Name")
    .require
    .bindAsText[Supervisor](
      _.firstName,
      (s, a) => s.copy(firstName = a),
    )

  private val lastName = UIAttributeBuilder.string
    .withLabel("Last Name")
    .require
    .bindAsText[Supervisor](
      _.lastName,
      (s, a) => s.copy(lastName = a),
    )

  private val eMail = UIAttributeBuilder.string
    .withLabel("Email")
    .require
    .bindAsText[Supervisor](
      _.eMail,
      (s, a) => s.copy(eMail = a),
    )
}
