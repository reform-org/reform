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
import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.entity.UIBasicAttribute
import de.tu_darmstadt.informatik.st.reform.entity.*
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.dateDiffMonth
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

case class ProjectsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Project](
      Title("Project"),
      None,
      jsImplicits.repositories.projects,
      jsImplicits.repositories.projects.all,
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

class ProjectAttributes(using jsImplicits: JSImplicits) {
  def name: UIAttribute[Project, String] = BuildUIAttribute().string
    .withLabel("Name")
    .require
    .bindAsText[Project](
      _.name,
      (p, a) => p.copy(name = a),
    )

  def maxHours: UIAttribute[Project, Int] = BuildUIAttribute().int
    .withLabel("Max Hours")
    .withMin("0")
    .withRegex("\\d*")
    .require
    .bindAsNumber[Project](
      _.maxHours,
      (p, a) => p.copy(maxHours = a),
    )

  def accountName: UIAttribute[Project, Option[String]] = BuildUIAttribute().string
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
        Signal.dynamic {
          jsImplicits.repositories.contracts.all.value
            .count(contract => contract.signal.value.contractAssociatedProject.option.contains(id))
            .toString + " Contract(s)"
        },
      readConverter = identity,
      formats = Seq(
        UIFormat(
          (id, project) => {
            Signal.dynamic {
              val contracts = jsImplicits.repositories.contracts.all.value.map(_.signal.value)
              !contracts.exists(contract => contract.contractAssociatedProject.option.contains(id))
            }
          },
          "text-slate-400 dark:text-gray-400 italic",
        ),
      ),
    )

  def countContractHours(
      id: String,
      project: Project,
      pred: (contractId: String, contract: Contract) => Boolean,
  ): Signal[Int] = {
    Signal.dynamic {
      jsImplicits.repositories.contracts.all.value
        .filter(contract => contract.signal.value.contractAssociatedProject.option.contains(id))
        .filter(contract => pred(contract.id, contract.signal.value))
        .map(x => {
          val contract = x.signal.value
          contract.contractHoursPerMonth.getOrElse(0) * dateDiffMonth(
            contract.contractStartDate.getOrElse(0L),
            contract.contractEndDate.getOrElse(0L),
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
            (id, contract) => !contract.isDraft.getOrElse(true),
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
            (id, contract) => contract.isDraft.getOrElse(true),
          ).value.toString + " h"
        },
      readConverter = identity,
    )
}
