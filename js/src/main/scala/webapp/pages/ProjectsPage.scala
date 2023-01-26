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

private val name: UIAttribute[Project, String] = UIAttribute(
  _._name,
  (p, a) => p.copy(_name = a),
  readConverter = identity,
  writeConverter = identity,
  placeholder = "Name",
  fieldType = "text",
  required = true,
)

private val maxHours: UIAttribute[Project, Int] = UIAttribute(
  _._maxHours,
  (p, a) => p.copy(_maxHours = a),
  readConverter = _.toString,
  writeConverter = _.toInt,
  placeholder = "Max Hours",
  fieldType = "number",
  required = true,
)

private val accountName: UIAttribute[Project, Option[String]] = UIAttribute(
  _._accountName,
  (p, a) => p.copy(_accountName = a),
  readConverter = _.getOrElse("no account"),
  writeConverter = Some(_),
  placeholder = "Account",
  fieldType = "text",
  required = false, // TODO FIXME this probably still has the not initialized error
)

case class ProjectsPage() extends EntityPage[Project](Repositories.projects, Seq(name, maxHours, accountName)) {}
