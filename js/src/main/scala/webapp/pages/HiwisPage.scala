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

private val hfirstName: UIAttribute[Hiwi, String] = UIAttribute(
  _._firstName,
  (p, a) => p.copy(_firstName = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "First Name",
  fieldType = "text",
  required = true,
)

private val hlastName: UIAttribute[Hiwi, String] = UIAttribute(
  _._lastName,
  (p, a) => p.copy(_lastName = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "Last Name",
  fieldType = "text",
  required = true,
)

private val hhours: UIAttribute[Hiwi, Int] = UIAttribute(
  _._hours,
  (p, a) => p.copy(_hours = a),
  readConverter = _.toString,
  writeConverter = _.toInt,
  placeholder = "Hours",
  fieldType = "number",
  required = true,
)

private val heMail: UIAttribute[Hiwi, String] = UIAttribute(
  _._eMail,
  (p, a) => p.copy(_eMail = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "Email",
  fieldType = "text",
  required = true,
)

case class HiwisPage() extends EntityPage[Hiwi](Repositories.hiwis, Seq(hfirstName, hlastName, hhours, heMail)) {}
