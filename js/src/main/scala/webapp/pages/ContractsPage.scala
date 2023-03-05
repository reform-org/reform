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
import webapp.services.{ToastMode, Toaster}
import webapp.components.common.*
import webapp.repo.Repository
import kofre.base.Bottom
import kofre.base.Lattice
import webapp.services.RoutingService
import webapp.npm.IIndexedDB
import ContractsPage.*
import webapp.utils.Seqnal.*
import webapp.repo.Synced
import outwatch.*
import outwatch.dsl.*
import webapp.given_ExecutionContext
import webapp.utils.Futures.*
import webapp.npm.JSUtils.toMoneyString

class DetailPageEntityRow[T <: Entity[T]](
    override val title: Title,
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
) extends EntityRow[T](title, repository, value, uiAttributes) {
  override protected def startEditing(): Unit = {
    value match {
      case Existing(value, editingValue) => routing.to(EditContractsPage(value.id))
      case New(value)                    =>
    }
  }

  override protected def afterCreated(id: String): Unit = routing.to(EditContractsPage(id))
}

class DetailPageEntityRowBuilder[T <: Entity[T]] extends EntityRowBuilder[T] {
  def construct(title: Title, repository: Repository[T], value: EntityValue[T], uiAttributes: Seq[UIBasicAttribute[T]])(
      using
      bottom: Bottom[T],
      lattice: Lattice[T],
      toaster: Toaster,
      routing: RoutingService,
      repositories: Repositories,
      indexedb: IIndexedDB,
  ): EntityRow[T] = DetailPageEntityRow(title, repository, value, uiAttributes)
}

def onlyFinalizedContracts(using repositories: Repositories): Signal[Seq[Synced[Contract]]] = {
  repositories.contracts.all.map(_.filterSignal(_.signal.map(!_.isDraft.get.getOrElse(true)))).flatten
}

case class ContractsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[Contract](
      Title("Contract"),
      None,
      repositories.contracts,
      onlyFinalizedContracts,
      Seq(
        contractAssociatedProject,
        contractAssociatedHiwi,
        contractAssociatedSupervisor,
        contractStartDate,
        contractEndDate,
        contractHoursPerMonth,
        moneyPerHour,
      ),
      DetailPageEntityRowBuilder(),
      true,
    ) {}

object ContractsPage {

  def contractAssociatedHiwi(using
      repositories: Repositories,
      routing: RoutingService,
      toaster: Toaster,
      indexeddb: IIndexedDB,
  ): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.hiwis.all.map(list =>
          list.map(value =>
            value.id -> value.signal.map(v => v.firstName.get.getOrElse("") + " " + v.lastName.get.getOrElse("")),
          ),
        ),
      )
      .withCreatePage(HiwisPage())
      .withLabel("Hiwi")
      .require
      .bindAsSelect(
        _.contractAssociatedHiwi,
        (p, a) => p.copy(contractAssociatedHiwi = a),
      )
  }

  def contractAssociatedProject(using
      repositories: Repositories,
      routing: RoutingService,
      toaster: Toaster,
      indexeddb: IIndexedDB,
  ): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.projects.all.map(_.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse("")))),
      )
      .withCreatePage(ProjectsPage())
      .withLabel("Project")
      .require
      .bindAsSelect(
        _.contractAssociatedProject,
        (p, a) => p.copy(contractAssociatedProject = a),
      )
  }

  def contractAssociatedSupervisor(using
      repositories: Repositories,
      routing: RoutingService,
      toaster: Toaster,
      indexeddb: IIndexedDB,
  ): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        options = repositories.supervisors.all.map(list =>
          list.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse(""))),
        ),
      )
      .withCreatePage(SupervisorsPage())
      .withLabel("Supervisor")
      .require
      .bindAsSelect(
        _.contractAssociatedSupervisor,
        (p, a) => p.copy(contractAssociatedSupervisor = a),
      )
  }

  def contractAssociatedType(using
      repositories: Repositories,
      routing: RoutingService,
      toaster: Toaster,
      indexeddb: IIndexedDB,
  ): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.contractSchemas.all.map(list =>
          list.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse(""))),
        ),
      )
      .withCreatePage(ContractSchemasPage())
      .withLabel("Type")
      .require
      .bindAsSelect(
        _.contractType,
        (p, a) => p.copy(contractType = a),
      )
  }

  def contractStartDate(using routing: RoutingService): UIAttribute[Contract, Long] = UIAttributeBuilder.date
    .withLabel("Start")
    .require
    .bindAsDatePicker[Contract](
      _.contractStartDate,
      (h, a) => h.copy(contractStartDate = a),
    )

  def contractEndDate(using routing: RoutingService): UIAttribute[Contract, Long] = UIAttributeBuilder.date
    .withLabel("End")
    .require
    .bindAsDatePicker[Contract](
      _.contractEndDate,
      (h, a) => h.copy(contractEndDate = a),
    )

  def contractHoursPerMonth(using routing: RoutingService): UIAttribute[Contract, Int] = UIAttributeBuilder.int
    .withLabel("h/month")
    .withMin("0")
    .require
    .bindAsNumber[Contract](
      _.contractHoursPerMonth,
      (h, a) => h.copy(contractHoursPerMonth = a),
    )

  def contractDraft(using routing: RoutingService): UIAttribute[Contract, Boolean] = UIAttributeBuilder.boolean
    .withLabel("Draft?")
    .require
    .bindAsCheckbox[Contract](
      _.isDraft,
      (h, a) => h.copy(isDraft = a),
    )

  def contractAssociatedPaymentLevel(using
      repositories: Repositories,
      routing: RoutingService,
      toaster: Toaster,
      indexeddb: IIndexedDB,
  ): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.paymentLevels.all.map(list =>
          list.map(value => value.id -> value.signal.map(v => v.title.get.getOrElse(""))),
        ),
      )
      .withCreatePage(PaymentLevelsPage())
      .withLabel("Payment Level")
      .require
      .bindAsSelect(
        _.contractAssociatedPaymentLevel,
        (p, a) => p.copy(contractAssociatedPaymentLevel = a),
      )
  }

  def requiredDocuments(using
      repositories: Repositories,
      routing: RoutingService,
      toaster: Toaster,
      indexeddb: IIndexedDB,
  ): UIAttribute[Contract, Seq[String]] = {
    UIAttributeBuilder
      .multiSelect(
        repositories.requiredDocuments.existing.map(list =>
          list.map(value => value.id -> value.signal.map(_.name.get.getOrElse(""))),
        ),
      )
      .withCreatePage(DocumentsPage())
      .withLabel("Required Documents")
      .require
      .bindAsMultiSelect[Contract](
        _.requiredDocuments,
        (c, a) => c.copy(requiredDocuments = a),
      )
  }

  def getMoneyPerHour(id: String, contract: Contract)(using repositories: Repositories): Signal[BigDecimal] =
    Signal.dynamic {
      val salaryChanges = repositories.salaryChanges.all.value
      salaryChanges
        .map(_.signal.value)
        .filter(p => Some(p.paymentLevel.get.getOrElse("")) == contract.contractAssociatedPaymentLevel.get)
        .filter(_.fromDate.get.getOrElse(0L) <= contract.contractStartDate.get.getOrElse(0L))
        .sortWith(_.fromDate.get.getOrElse(0L) > _.fromDate.get.getOrElse(0L))
        .headOption match {
        case None     => BigDecimal(0)
        case Some(sc) => sc.value.get.getOrElse(BigDecimal(0))
      }
    }

  def getMoneyPerHourShould(id: String, contract: Contract)(using repositories: Repositories): Signal[BigDecimal] =
    Signal.dynamic {
      val salaryChanges = repositories.salaryChanges.all.value
      salaryChanges
        .map(_.signal.value)
        .filter(p => Some(p.paymentLevel.get.getOrElse("")) == contract.contractAssociatedPaymentLevel.get)
        .sortWith(_.fromDate.get.getOrElse(0L) > _.fromDate.get.getOrElse(0L))
        .headOption match {
        case None     => BigDecimal(0)
        case Some(sc) => sc.value.get.getOrElse(BigDecimal(0))
      }
    }

  def moneyPerHour(using repositories: Repositories) =
    new UIReadOnlyAttribute[Contract, String](
      label = "€/h",
      getter = (id, contract) => Signal { toMoneyString(getMoneyPerHour(id, contract).value) },
      readConverter = identity,
      formats = Seq(
        UIFormat(
          (id, contract) =>
            Signal {
              getMoneyPerHour(id, contract).value != getMoneyPerHourShould(id, contract).value
            },
          "text-red-500 font-bold",
        ),
      ),
    )
}
