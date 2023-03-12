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
import webapp.services.RoutingService
import webapp.npm.IIndexedDB
import webapp.npm.JSUtils.dateDiffMonth
import webapp.services.MailService
import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService
import webapp.JSImplicits

case class ProjectsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Project](
      Title("Project"),
      None,
      repositories.projects,
      repositories.projects.all,
      Seq[UIBasicAttribute[Project]](
        ProjectAttributes().name,
        ProjectAttributes().maxHours,
        ProjectAttributes().accountName,
        ProjectAttributes().contractCount,
        ProjectAttributes().plannedHours,
        ProjectAttributes().assignedHours,
      ),
      DefaultEntityRow(),
    ) {}

class ProjectAttributes(using routing: RoutingService, repositories: Repositories) {
  def name = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[Project](
      _.name,
      (p, a) => p.copy(name = a),
    )

  def maxHours = BuildUIAttribute().int
    .withLabel("Max Hours")
    .withMin("0")
    .require
    .bindAsNumber[Project](
      _.maxHours,
      (p, a) => p.copy(maxHours = a),
    )

  def accountName = BuildUIAttribute().string
    .withLabel("Account")
    .withDefaultValue("")
    .bindAsText[Project](
      _.accountName,
      (p, a) => p.copy(accountName = a),
    )

  def contractCount =
    new UIReadOnlyAttribute[Project, String](
      label = "Contracts",
      getter = (id, project) =>
        repositories.contracts.all
          .map(_.map(_.signal))
          .flatten
          .map(contracts =>
            contracts.count(contract => contract.contractAssociatedProject.get.contains(id)).toString + " Contract(s)",
          ),
      readConverter = identity,
      formats = Seq(
        UIFormat(
          (id, project) => {
            Signal.dynamic {
              val contracts = repositories.contracts.all.value.map(_.signal.value)
              contracts.filter(contract => contract.contractAssociatedProject.get == Some(id)).size == 0
            }
          },
          "text-slate-400 italic",
        ),
      ),
    )

  def countContractHours(
      id: String,
      project: Project,
      pred: (contractId: String, contract: Contract) => Boolean,
  ): Signal[Int] = {
    Signal.dynamic {
      repositories.contracts.all.value
        .filter(contract => contract.signal.value.contractAssociatedProject.get.contains(id))
        .filter(contract => pred(contract.id, contract.signal.value))
        .map(x => {
          val contract = x.signal.value
          contract.contractHoursPerMonth.get.getOrElse(0) * dateDiffMonth(
            contract.contractStartDate.get.getOrElse(0L),
            contract.contractEndDate.get.getOrElse(0L),
          )
        })
        .fold[Int](0)((acc: Int, x: Int) => acc + x)
    }
  }

  def assignedHours =
    new UIReadOnlyAttribute[Project, String](
      label = "assigned Hours",
      getter = (id, project) =>
        Signal {
          countContractHours(
            id,
            project,
            (id, contract) => !contract.isDraft.get.getOrElse(true),
          ).value.toString + " h"
        },
      readConverter = identity,
    )

  def plannedHours =
    new UIReadOnlyAttribute[Project, String](
      label = "planned Hours",
      getter = (id, project) =>
        Signal {
          countContractHours(
            id,
            project,
            (id, contract) => contract.isDraft.get.getOrElse(true),
          ).value.toString + " h"
        },
      readConverter = identity,
    )
}
