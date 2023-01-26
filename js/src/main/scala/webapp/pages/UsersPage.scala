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

private val username: UIAttribute[User, String] = UIAttribute(
  _._username,
  (p, a) => p.copy(_username = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "Username",
  fieldType = "text",
  required = true,
)

private val role: UIAttribute[User, String] = UIAttribute(
  _._role,
  (p, a) => p.copy(_role = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "Role",
  fieldType = "text",
  required = true,
)

private val comment: UIAttribute[User, Option[String]] = UIAttribute(
  _._comment,
  (p, a) => p.copy(_comment = a),
  readConverter = _.getOrElse("no comment"),
  writeConverter = Some(_),
  placeholder = "Comment",
  fieldType = "text",
  required = false, // TODO FIXME this probably still has the not initialized issue
)

case class UsersPage() extends EntityPage[User](Repositories.users, Seq(username, role, comment)) {}
