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
  placeholder = "Name"
)

case class ProjectsPage() extends EntityPage[Project](Repositories.projects, Seq(name)) {

  /* override def getUIAttributes(project: Project): Seq[UIAttribute[Project, _]] =
    Seq(
      UIAttribute(
        project._name,
        AttributeHandler.string((p, n) =>
          p.copy(_name = p._name.set(n))
        ),
        "Name"
      ),
      UIAttribute(
        project._maxHours,
        AttributeHandler.int((p, x) =>
          p.copy(_maxHours = p._maxHours.set(x))
        ),
        "Max Hours"
      ),
      UIAttribute(
        project._accountName,
        AttributeHandler.optionWithDefault("no account",
          AttributeHandler.string((p, x) =>
            p.copy(_accountName = p._accountName.set(x))
          )),
        "Account Name"
      )
    ) */
}
