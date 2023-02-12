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

import HiwisPage.*

case class HiwisPage()(using repositories: Repositories, toaster: Toaster)
    extends EntityPage[Hiwi](
      repositories.hiwis,
      Seq(firstName, lastName, hours, eMail, birthdate),
    ) {}

object HiwisPage {
  private val firstName = UIAttributeBuilder.string
    .withLabel("First Name")
    .require
    .bindAsText[Hiwi](
      _.firstName,
      (h, a) => h.copy(firstName = a),
    )

  private val lastName = UIAttributeBuilder.string
    .withLabel("Last Name")
    .require
    .bindAsText[Hiwi](
      _.lastName,
      (h, a) => h.copy(lastName = a),
    )

  private val hours = UIAttributeBuilder.int
    .withLabel("Hours")
    .require
    .bindAsNumber[Hiwi](
      _.hours,
      (h, a) => h.copy(hours = a),
    )

  private val eMail = UIAttributeBuilder.string
    .withLabel("Email")
    .require
    .bindAsText[Hiwi](
      _.eMail,
      (h, a) => h.copy(eMail = a),
    )

  private val birthdate = UIAttributeBuilder.date
    .withLabel("Birthdate")
    .require
    .bindAsDatePicker[Hiwi](
      _.birthdate,
      (h, a) => h.copy(birthdate = a),
    )
}
