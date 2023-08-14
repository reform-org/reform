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
package de.tu_darmstadt.informatik.st.reform.pages

import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.entity.*

import HiwisPage.*

case class HiwisPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Hiwi](
      Title("Hiwi"),
      None,
      jsImplicits.repositories.hiwis,
      jsImplicits.repositories.hiwis.all,
      Seq(HiwiAttributes().firstName, HiwiAttributes().lastName, HiwiAttributes().eMail, HiwiAttributes().birthdate),
      DefaultEntityRow(),
    ) {}

class HiwiAttributes(using jsImplicits: JSImplicits) {
  def firstName: UIAttribute[Hiwi, String] = BuildUIAttribute().string
    .withLabel("First Name")
    .require
    .bindAsText[Hiwi](
      _.firstName,
      (h, a) => h.copy(firstName = a),
    )

  def lastName: UIAttribute[Hiwi, String] = BuildUIAttribute().string
    .withLabel("Last Name")
    .require
    .bindAsText[Hiwi](
      _.lastName,
      (h, a) => h.copy(lastName = a),
    )

  def eMail: UIAttribute[Hiwi, String] = BuildUIAttribute().email
    .withLabel("Email")
    .require
    .bindAsText[Hiwi](
      _.eMail,
      (h, a) => h.copy(eMail = a),
    )

  def birthdate: UIAttribute[Hiwi, Long] = BuildUIAttribute().date
    .withLabel("Birthdate")
    .bindAsDatePicker[Hiwi](
      _.birthdate,
      (h, a) => h.copy(birthdate = a),
    )
}
