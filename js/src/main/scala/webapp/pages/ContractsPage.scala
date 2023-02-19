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
import rescala.default.*
import webapp.services.Toaster
import webapp.components.common.*

private def contractAssociatedHiwi(using repositories: Repositories): UISelectAttribute[Contract, String] =
  UISelectAttribute(
    _.contractAssociatedHiwi,
    (p, a) => p.copy(contractAssociatedHiwi = a),
    readConverter = identity,
    writeConverter = identity,
    label = "AssociatedHiwi",
    options = repositories.hiwis.all.map(list =>
      list.map(value =>
        new SelectOption[Signal[String]](value.id, value.signal.map(v => v.firstName.get.getOrElse(""))),
      ),
    ),
    isRequired = true,
  )

private def contractAssociatedProject(using repositories: Repositories): UISelectAttribute[Contract, String] =
  UISelectAttribute(
    _.contractAssociatedProject,
    (p, a) => p.copy(contractAssociatedProject = a),
    readConverter = identity,
    writeConverter = identity,
    label = "Project",
    options = repositories.projects.all.map(list =>
      list.map(value =>
        new SelectOption[Signal[String]](
          value.id,
          value.signal.map(v => v.name.get.getOrElse("")),
        ),
      ),
    ),
    isRequired = true,
  )

case class ContractsPage()(using repositories: Repositories, toaster: Toaster)
    extends EntityPage[Contract](
      repositories.contracts,
      Seq(
        contractAssociatedProject,
        contractAssociatedHiwi,
      ),
    ) {}
