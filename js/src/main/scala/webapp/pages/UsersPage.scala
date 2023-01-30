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

private val username = UIAttributeBuilder.string
  .withLabel("Username")
  .bind[User](
    _.username,
    (p, a) => p.copy(username = a),
  )

private val role = UIAttributeBuilder.string
  .withLabel("Role")
  .bind[User](
    _.role,
    (p, a) => p.copy(role = a),
  )

private val comment = UIAttributeBuilder.string
  .withLabel("Comment")
  .withDefaultValue("")
  .bind[User](
    _.comment,
    (p, a) => p.copy(comment = a),
  )

case class UsersPage()(using repositories: Repositories)
    extends EntityPage[User](repositories.users, Seq(username, role, comment)) {}
