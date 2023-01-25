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

private val firstName = UIAttributeBuilder.string
  .withPlaceholder("First Name")
  .bind[Hiwi](
    _.firstName,
    (p, a) => p.copy(firstName = a),
  )

private val lastName = UIAttributeBuilder.string
  .withPlaceholder("Last Name")
  .bind[Hiwi](
    _.lastName,
    (p, a) => p.copy(lastName = a),
  )

private val hours = UIAttributeBuilder.int
  .withPlaceholder("Hours")
  .bind[Hiwi](
    _.hours,
    (p, a) => p.copy(hours = a),
  )

private val eMail = UIAttributeBuilder.string
  .withPlaceholder("Email")
  .bind[Hiwi](
    _.eMail,
    (p, a) => p.copy(eMail = a),
  )

case class HiwisPage() extends EntityPage[Hiwi](Repositories.hiwis, Seq(firstName, lastName, hours, eMail)) {}
