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
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.entity.*
import de.tu_darmstadt.informatik.st.reform.repo.Repository
import de.tu_darmstadt.informatik.st.reform.repo.Synced
import kofre.base.Bottom
import kofre.base.Lattice
import outwatch.dsl.*
import rescala.default.*

def onlyDrafts(using jsImplicits: JSImplicits): Signal[Seq[Synced[Contract]]] = Signal.dynamic {
  jsImplicits.repositories.contracts.all.value.filter(_.signal.value.isDraft.get.getOrElse(true))
}

class DraftDetailPageEntityRow[T <: Entity[T]](
    override val title: Title,
    override val repository: Repository[T],
    override val value: EntityValue[T],
    override val uiAttributes: Seq[UIBasicAttribute[T]],
)(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    jsImplicits: JSImplicits,
) extends EntityRow[T](title, repository, value, uiAttributes) {
  override protected def startEditing(): Unit = {
    value match {
      case Existing(value, editingValue) => jsImplicits.routing.to(EditContractsPage(value.id))
      case New(value)                    =>
    }
  }
}

class DraftDetailPageEntityRowBuilder[T <: Entity[T]] extends EntityRowBuilder[T] {
  def construct(title: Title, repository: Repository[T], value: EntityValue[T], uiAttributes: Seq[UIBasicAttribute[T]])(
      using
      bottom: Bottom[T],
      lattice: Lattice[T],
      jsImplicits: JSImplicits,
  ): EntityRow[T] = DraftDetailPageEntityRow(title, repository, value, uiAttributes)
}

case class ContractDraftsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Contract](
      Title("Contract Draft"),
      None,
      jsImplicits.repositories.contracts,
      onlyDrafts,
      Seq(
        ContractPageAttributes().contractAssociatedProject,
        ContractPageAttributes().contractAssociatedHiwi,
        ContractPageAttributes().contractAssociatedSupervisor,
        ContractPageAttributes().contractStartDate,
        ContractPageAttributes().contractEndDate,
        ContractPageAttributes().signed,
        ContractPageAttributes().submitted,
        ContractPageAttributes().moneyPerHour,
        ContractPageAttributes().contractHoursPerMonth,
        ContractDraftAttributes().forms,
      ),
      DraftDetailPageEntityRowBuilder(),
      true,
      Button(
        ButtonStyle.LightPrimary,
        "Add new contract draft",
        onClick.foreach(_ => jsImplicits.routing.to(NewContractPage())),
        cls := "!mt-0",
      ),
    ) {}

class ContractDraftAttributes(using jsImplicits: JSImplicits) {
  def countForms(contract: Contract, predicate: String => Boolean): Signal[Int] =
    Signal.dynamic {
      val contractTypeId = contract.contractType.get.getOrElse("")
      val contractSchema =
        jsImplicits.repositories.contractSchemas.all.value.find(contractSchema => contractSchema.id == contractTypeId)
      contractSchema.flatMap(_.signal.value.files.get).getOrElse(Seq.empty).count(predicate)
    }

  def forms =
    new UIReadOnlyAttribute[Contract, String](
      label = "Forms",
      getter = (id, contract) =>
        Signal {
          s"${countForms(contract, id => contract.requiredDocuments.get.getOrElse(Seq.empty).contains(id)).value} of ${countForms(contract, _ => true).value}"
        },
      readConverter = identity,
      formats = Seq(
        UIFormat(
          (_, contract) =>
            Signal {
              countForms(
                contract,
                id => contract.requiredDocuments.get.getOrElse(Seq.empty).contains(id),
              ).value == countForms(contract, _ => true).value
            },
          "bg-green-200 text-green-600 justify-center",
        ),
        UIFormat(
          (_, contract) =>
            Signal {
              countForms(
                contract,
                id => contract.requiredDocuments.get.getOrElse(Seq.empty).contains(id),
              ).value != countForms(contract, _ => true).value
            },
          "bg-red-200 text-red-600 justify-center",
        ),
      ),
    )
}
