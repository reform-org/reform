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

def onlyDrafts(using repositories: Repositories): Signal[Seq[Synced[Contract]]] = {
  repositories.contracts.all.map(_.filterSignal(_.signal.map(_.isDraft.get.getOrElse(true)))).flatten
}

case class ContractDraftsPage()(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
    indexedb: IIndexedDB,
) extends EntityPage[Contract](
      "Contract drafts",
      repositories.contracts,
      onlyDrafts,
      Seq(
        contractAssociatedProject,
        contractAssociatedHiwi,
        contractAssociatedSupervisor,
        contractStartDate,
        contractEndDate,
        // ContractDraftsPage.moneyPerHour,
        hoursPerMonth,
        ContractDraftsPage.forms,
      ),
      DetailPageEntityRowBuilder(),
    ) {}

object ContractDraftsPage {
  // private def moneyPerHour(using repositories: Repositories) =
  //   new UIReadOnlyAttribute[Contract, String](
  //     label = "€/h",
  //     getter = (id, contract) => {
  //       repositories.paymentLevels.all
  //         .map(paymentLevels => {
  //           val paymentLevel = paymentLevels.find(p => Some(p.id) == contract.contractAssociatedPaymentLevel.get)
  //           paymentLevel match {
  //             case None => Signal("")
  //             case Some(paymentLevel) => {
  //               repositories.salaryChanges.all
  //                 .map(salaryChanges => {
  //                   val latestSalaryChange = salaryChanges
  //                     .map(_.signal)
  //                     .filterSignal(salaryChange =>
  //                       salaryChange.map(salaryChange =>
  //                         salaryChange.paymentLevel.get == Some(paymentLevel.id) && salaryChange.fromDate.get == Some(
  //                           1677625200000L,
  //                         ),
  //                       ),
  //                     )
  //                     .map(a => a(0).map(b => b.value.get))
  //                     .flatten

  //                   latestSalaryChange.map(a => a.getOrElse(0L).toString())
  //                 })
  //             }
  //           }
  //         })
  //         .flatten
  //     },
  //     readConverter = identity,
  //   )

  private def forms(using repositories: Repositories) =
    new UIReadOnlyAttribute[Contract, String](
      label = "Forms",
      getter = (id, contract) => {
        val contractTypeId = contract.contractType.get.getOrElse("")
        val contractSchema = repositories.contractSchemas.all.map(contractSchemas =>
          contractSchemas.find(contractSchema => contractSchema.id == contractTypeId),
        )
        val totalDocumentCount = contractSchema
          .map(contractSchema => {
            contractSchema match {
              case None        => Signal("")
              case Some(value) => value.signal.map(v => v.files.get.size.toString())
            }
          })
          .flatten

        totalDocumentCount.map(docs => s"0 / $docs")
      },
      readConverter = identity,
    )
}
