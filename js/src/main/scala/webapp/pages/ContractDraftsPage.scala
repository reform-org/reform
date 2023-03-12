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
import ContractsPage.*
import webapp.utils.Seqnal.*
import webapp.repo.Synced
import outwatch.dsl.*
import webapp.npm.JSUtils.toMoneyString
import webapp.services.MailService

def onlyDrafts(using repositories: Repositories): Signal[Seq[Synced[Contract]]] = {
  repositories.contracts.all.map(_.filterSignal(_.signal.map(_.isDraft.get.getOrElse(true)))).flatten
}

case class ContractDraftsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[Contract](
      Title("Contract Draft"),
      None,
      repositories.contracts,
      onlyDrafts,
      Seq(
        contractAssociatedProject,
        contractAssociatedHiwi,
        contractAssociatedSupervisor,
        contractStartDate,
        contractEndDate,
        signed,
        submitted,
        moneyPerHour,
        contractHoursPerMonth,
        ContractDraftsPage.forms,
      ),
      DetailPageEntityRowBuilder(),
      true,
      Button(
        ButtonStyle.LightPrimary,
        "Add new contract draft",
        onClick.foreach(_ => routing.to(NewContractPage())),
        cls := "!mt-0",
      ),
    ) {}

object ContractDraftsPage {
  private def countForms(contract: Contract, predicate: String => Boolean)(using
      repositories: Repositories,
  ): Signal[Int] =
    Signal.dynamic {
      val contractTypeId = contract.contractType.get.getOrElse("")
      val contractSchema =
        repositories.contractSchemas.all.value.find(contractSchema => contractSchema.id == contractTypeId)
      contractSchema.flatMap(_.signal.value.files.get).getOrElse(Seq.empty).count(predicate)
    }

  private def forms(using repositories: Repositories) =
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
          "text-green-500 font-bold",
        ),
        UIFormat(
          (_, contract) =>
            Signal {
              countForms(
                contract,
                id => contract.requiredDocuments.get.getOrElse(Seq.empty).contains(id),
              ).value != countForms(contract, _ => true).value
            },
          "text-red-500 font-bold",
        ),
      ),
    )
}
