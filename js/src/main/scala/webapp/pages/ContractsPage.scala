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
import webapp.repo.Repository
import kofre.base.Bottom
import kofre.base.Lattice
import webapp.services.RoutingService
import webapp.npm.IIndexedDB

private def contractAssociatedHiwi(using
    repositories: Repositories,
    routing: RoutingService,
): UIAttribute[Contract, String] = {
  UIAttributeBuilder
    .select(
      repositories.hiwis.all.map(list =>
        list.map(value => value.id -> value.signal.map(v => v.firstName.get.getOrElse(""))),
      ),
    )
    .withLabel("Associated Hiwi")
    .require
    .bindAsSelect(
      _.contractAssociatedHiwi,
      (p, a) => p.copy(contractAssociatedHiwi = a),
    )
}

private def contractAssociatedProject(using
    repositories: Repositories,
    routing: RoutingService,
): UIAttribute[Contract, String] = {
  UIAttributeBuilder
    .select(
      repositories.projects.all.map(_.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse("")))),
    )
    .withLabel("Project")
    .require
    .bindAsSelect(
      _.contractAssociatedProject,
      (p, a) => p.copy(contractAssociatedProject = a),
    )
}

class DetailPageEntityRow[T <: Entity[T]](
    override val repository: Repository[T],
    override val value: EntityValue[T],
    override val uiAttributes: Seq[UIBasicAttribute[T]],
)(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    toaster: Toaster,
    routing: RoutingService,
    repositories: Repositories,
    indexedb: IIndexedDB,
) extends EntityRow[T](repository, value, uiAttributes) {
  override protected def startEditing(): Unit = {
    value match {
      case Existing(value, editingValue) => routing.to(EditContractsPage(value.id))
      case New(value)                    => {}
    }
  }

  override protected def afterCreated(id: String): Unit = routing.to(EditContractsPage(id))
}

class DetailPageEntityRowBuilder[T <: Entity[T]] extends EntityRowBuilder[T] {
  def construct(repository: Repository[T], value: EntityValue[T], uiAttributes: Seq[UIBasicAttribute[T]])(using
      bottom: Bottom[T],
      lattice: Lattice[T],
      toaster: Toaster,
      routing: RoutingService,
      repositories: Repositories,
      indexedb: IIndexedDB,
  ): EntityRow[T] = DetailPageEntityRow(repository, value, uiAttributes)
}

case class ContractsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[Contract](
      "Contract",
      repositories.contracts,
      Seq(
        contractAssociatedProject,
        contractAssociatedHiwi,
      ),
      DetailPageEntityRowBuilder(),
    ) {}
