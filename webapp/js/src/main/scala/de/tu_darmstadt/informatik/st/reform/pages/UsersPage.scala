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

import UsersPage.*

case class UsersPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[User](
      Title("User"),
      None,
      jsImplicits.repositories.users,
      jsImplicits.repositories.users.all,
      Seq(UserAttributes().username, UserAttributes().role, UserAttributes().comment),
      DefaultEntityRow(),
    ) {}

class UserAttributes(using jsImplicits: JSImplicits) {
  def username = BuildUIAttribute().string
    .withLabel("Username")
    .require
    .bindAsText[User](
      _.username,
      (u, a) => u.copy(username = a),
    )

  def role = BuildUIAttribute().string
    .withLabel("Role")
    .require
    .bindAsText[User](
      _.role,
      (u, a) => u.copy(role = a),
    )

  def comment = BuildUIAttribute().string
    .withLabel("Comment")
    .withDefaultValue("")
    .bindAsText[User](
      _.comment,
      (u, a) => u.copy(comment = a),
    )
}
