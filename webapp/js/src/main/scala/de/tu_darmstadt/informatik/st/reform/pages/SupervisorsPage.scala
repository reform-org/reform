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

import SupervisorsPage.*

case class SupervisorsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Supervisor](
      Title("Supervisor"),
      None,
      jsImplicits.repositories.supervisors,
      jsImplicits.repositories.supervisors.all,
      Seq(SupervisorAttributes().name, SupervisorAttributes().eMail),
      DefaultEntityRow(),
    ) {}

class SupervisorAttributes(using jsImplicits: JSImplicits) {
  def name = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[Supervisor](
      _.name,
      (s, a) => s.copy(name = a),
    )

  def eMail = BuildUIAttribute().email
    .withLabel("Email")
    .require
    .bindAsText[Supervisor](
      _.eMail,
      (s, a) => s.copy(eMail = a),
    )
}
