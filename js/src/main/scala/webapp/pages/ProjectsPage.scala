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
import webapp.services.Toaster
import ProjectsPage.*
import webapp.utils.Seqnal.*
import webapp.entity.UIBasicAttribute
import outwatch.*
import outwatch.dsl.*
import webapp.{*, given}
import rescala.default.*

case class ProjectsPage()(using repositories: Repositories, toaster: Toaster)
    extends EntityPage[Project](
      repositories.projects,
      Seq[UIBasicAttribute[Project]](ProjectsPage.name, maxHours, accountName, contractCount),
    ) {}

object ProjectsPage {
  private val name = UIAttributeBuilder.string
    .withLabel("Name")
    .require
    .bindAsText[Project](
      _.name,
      (p, a) => p.copy(name = a),
    )

  private val maxHours = UIAttributeBuilder.int
    .withLabel("Max Hours")
    .require
    .bindAsNumber[Project](
      _.maxHours,
      (p, a) => p.copy(maxHours = a),
    )

  private val accountName = UIAttributeBuilder.string
    .withLabel("Account")
    .withDefaultValue("")
    .bindAsText[Project](
      _.accountName,
      (p, a) => p.copy(accountName = a),
    )

  private val nameLength = UIAttributeBuilder.int
    .withLabel("Length of the Name")
    .map[String](_.length, _.toString)
    .bindReadOnly[Project](_.name)

  private def contractCount(using repositories: Repositories) = new UIBasicAttribute[Project](
    label = "Contracts",
  ) {

    override def render(entity: Project): VNode = {
      div(repositories.contracts.all.mapInside(contract => {
        contract.signal.map(_.contractAssociatedProject == entity)
      }))
    }

    def renderEdit(formId: String, entityVar: Var[Option[Project]]): Signal[VNode] = Signal(div())
  }
}
