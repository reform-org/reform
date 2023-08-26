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
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.dateDiffMonth
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.getMonth
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.getYear
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.toMoneyString
import de.tu_darmstadt.informatik.st.reform.repo.Repository
import de.tu_darmstadt.informatik.st.reform.repo.Synced
import kofre.base.Bottom
import kofre.base.Lattice
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

import scala.annotation.nowarn
import scala.scalajs.js

class DetailPageEntityRow[T <: Entity[T]](
    override val title: Title,
    override val repository: Repository[T],
    override val value: EntityValue[T],
    override val uiAttributes: Seq[UIBasicAttribute[T]],
)(using
    bottom: Bottom[T],
    lattice: Lattice[T],
    jsImplicits: JSImplicits,
) extends EntityRow[T](title, repository, value, uiAttributes) {

  override protected val editLabel: String = "Extend"
  override protected def startEditing(): Unit = {
    value match {
      case Existing(value, editingValue) => jsImplicits.routing.to(ExtendContractPage(value.id))
      case New(value)                    =>
    }
  }
}

class DetailPageEntityRowBuilder[T <: Entity[T]] extends EntityRowBuilder[T] {
  def construct(title: Title, repository: Repository[T], value: EntityValue[T], uiAttributes: Seq[UIBasicAttribute[T]])(
      using
      bottom: Bottom[T],
      lattice: Lattice[T],
      jsImplicits: JSImplicits,
  ): EntityRow[T] = DetailPageEntityRow(title, repository, value, uiAttributes)
}

def onlyFinalizedContracts(using jsImplicits: JSImplicits): Signal[Seq[Synced[Contract]]] = Signal.dynamic {
  jsImplicits.repositories.contracts.all.value.filter(!_.signal.value.isDraft.get.getOrElse(true))
}

case class ContractsPage()(using
    jsImplicits: JSImplicits,
) extends EntityPage[Contract](
      Title("Contract"),
      None,
      jsImplicits.repositories.contracts,
      onlyFinalizedContracts,
      Seq(
        ContractPageAttributes().contractAssociatedProject,
        ContractPageAttributes().contractAssociatedHiwi,
        ContractPageAttributes().contractAssociatedSupervisor,
        ContractPageAttributes().contractStartDate,
        ContractPageAttributes().contractEndDate,
        ContractPageAttributes().contractHoursPerMonth,
        ContractPageAttributes().moneyPerHour,
      ),
      DetailPageEntityRowBuilder(),
      true,
    ) {}

class ContractPageAttributes(using
    jsImplicits: JSImplicits,
) {

  def contractAssociatedHiwi: UIAttribute[Contract, String] = {
    BuildUIAttribute()
      .select(
        Signal {
          jsImplicits.repositories.hiwis.existing.value.map(value =>
            SelectOption(value.id, value.signal.map(_.identifier.get.getOrElse(""))),
          )
        },
      )
      .withCreatePage(HiwisPage())
      .withLabel("Hiwi")
      .require
      .bindAsSelect(
        _.contractAssociatedHiwi,
        (p, a) => p.copy(contractAssociatedHiwi = a),
      )
  }

  def contractAssociatedProject: UIAttribute[Contract, String] = {
    BuildUIAttribute()
      .select(options = Signal {
        jsImplicits.repositories.projects.existing.value.map(value =>
          SelectOption(value.id, value.signal.map(_.identifier.get.getOrElse(""))),
        )
      })
      .withCreatePage(ProjectsPage())
      .withLabel("Project")
      .require
      .bindAsSelect(
        _.contractAssociatedProject,
        (p, a) => p.copy(contractAssociatedProject = a),
      )
  }

  def contractAssociatedSupervisor: UIAttribute[Contract, String] = {
    BuildUIAttribute()
      .select(
        Signal {
          jsImplicits.repositories.supervisors.existing.value.map(value =>
            SelectOption(value.id, value.signal.map(_.identifier.get.getOrElse(""))),
          )
        },
      )
      .withCreatePage(SupervisorsPage())
      .withLabel("Supervisor")
      .require
      .bindAsSelect(
        _.contractAssociatedSupervisor,
        (p, a) => p.copy(contractAssociatedSupervisor = a),
      )
  }

  def contractAssociatedType: UIAttribute[Contract, String] = {
    BuildUIAttribute()
      .select(
        Signal {
          jsImplicits.repositories.contractSchemas.existing.value.map(value =>
            SelectOption(value.id, value.signal.map(_.identifier.get.getOrElse(""))),
          )
        },
      )
      .withCreatePage(ContractSchemasPage())
      .withLabel("Type")
      .require
      .bindAsSelect(
        _.contractType,
        (p, a) => p.copy(contractType = a),
      )
  }

  def contractStartDate: UIAttribute[Contract, Long] = BuildUIAttribute().date
    .withLabel("Start")
    .require
    .bindAsDatePicker[Contract](
      _.contractStartDate,
      (h, a) => h.copy(contractStartDate = a),
    )

  def contractEndDate: UIAttribute[Contract, Long] = BuildUIAttribute().date
    .withLabel("End")
    .require
    .bindAsDatePicker[Contract](
      _.contractEndDate,
      (h, a) => h.copy(contractEndDate = a),
    )

  def contractSentDate: UIAttribute[Contract, Long] = BuildUIAttribute().date
    .withLabel("Contract Sent")
    .require
    .bindAsDatePicker[Contract](
      _.contractSentDate,
      (h, a) => h.copy(contractSentDate = a),
    )

  def letterSentDate: UIAttribute[Contract, Long] = BuildUIAttribute().date
    .withLabel("Letter Sent")
    .require
    .bindAsDatePicker[Contract](
      _.letterSentDate,
      (h, a) => h.copy(letterSentDate = a),
    )

  def reminderSentDate: UIAttribute[Contract, Long] = BuildUIAttribute().date
    .withLabel("Reminder Sent")
    .require
    .bindAsDatePicker[Contract](
      _.reminderSentDate,
      (h, a) => h.copy(reminderSentDate = a),
    )

  def contractHoursPerMonth: UIAttribute[Contract, Int] = BuildUIAttribute().int
    .withLabel("h/month")
    .withMin("0")
    .withRegex("\\d*")
    .require
    .bindAsNumber[Contract](
      _.contractHoursPerMonth,
      (h, a) => h.copy(contractHoursPerMonth = a),
    )

  def contractDraft: UIAttribute[Contract, Boolean] = BuildUIAttribute().boolean
    .withLabel("Draft?")
    .require
    .bindAsCheckbox[Contract](
      _.isDraft,
      (h, a) => h.copy(isDraft = a),
    )

  def signed: UIAttribute[Contract, Boolean] = BuildUIAttribute().boolean
    .withLabel("Signed?")
    .require
    .bindAsCheckbox[Contract](
      _.isSigned,
      (h, a) => h.copy(isSigned = a),
    )

  def submitted: UIAttribute[Contract, Boolean] = BuildUIAttribute().boolean
    .withLabel("Submitted?")
    .require
    .bindAsCheckbox[Contract](
      _.isSubmitted,
      (h, a) => h.copy(isSubmitted = a),
    )

  def contractAssociatedPaymentLevel: UIAttribute[Contract, String] = {
    BuildUIAttribute()
      .select(
        Signal {
          jsImplicits.repositories.paymentLevels.existing.value.map(value =>
            SelectOption(value.id, value.signal.map(_.identifier.get.getOrElse(""))),
          )
        },
      )
      .withCreatePage(PaymentLevelsPage())
      .withLabel("Payment Level")
      .require
      .bindAsSelect(
        _.contractAssociatedPaymentLevel,
        (p, a) => p.copy(contractAssociatedPaymentLevel = a),
      )
  }

  def requiredDocuments: UIAttribute[Contract, Seq[String]] = {
    BuildUIAttribute()
      .checkboxList(
        Signal {
          jsImplicits.repositories.requiredDocuments.existing.value.map(value =>
            SelectOption(value.id, value.signal.map(_.identifier.get.getOrElse(""))),
          )
        },
      )
      .withLabel("Required Documents")
      .require
      .bindAsCheckboxList[Contract](
        _.requiredDocuments,
        (c, a) => c.copy(requiredDocuments = a),
        filteredOptions = Some(contract =>
          Signal.dynamic {
            contract.contractType.get
              .flatMap(contractTypeId =>
                jsImplicits.repositories.contractSchemas.all.value
                  .find(contractType => contractType.id == contractTypeId)
                  .flatMap(value =>
                    value.signal.value.files.get.flatMap(requiredDocuments => {
                      val documents = jsImplicits.repositories.requiredDocuments.all.value
                      val checkedDocuments =
                        if (contract.requiredDocuments.get.nonEmpty) contract.requiredDocuments.get
                        else Some(Seq.empty)

                      checkedDocuments
                        .map(_ ++ requiredDocuments)
                        .map(files =>
                          files.toSet
                            .map(fileId => {
                              documents
                                .find(doc => doc.id == fileId)
                                .map(file => {
                                  SelectOption(
                                    fileId,
                                    Signal { file.signal.value.name.get.getOrElse("") },
                                    if (!requiredDocuments.contains(fileId)) Seq(cls := "italic", checked := true)
                                    else None,
                                  )
                                })
                            })
                            .toSeq
                            .sortWith(
                              _.getOrElse(SelectOption("", Signal(""))).id < _.getOrElse(
                                SelectOption("", Signal("")),
                              ).id,
                            ),
                        )
                    }),
                  ),
              )
              .getOrElse(Seq.empty)
              .filter(x => x.nonEmpty)
              .map(_.get)
          },
        ),
      )
  }

  def isInInterval(contract: Contract, month: Int, year: Int): Boolean = {
    if (contract.contractStartDate.get.nonEmpty && contract.contractEndDate.get.nonEmpty) {
      val start = contract.contractStartDate.get.get
      val end = contract.contractEndDate.get.get

      if (
        (getYear(start) < year || getYear(start) == year && getMonth(start) <= month) && (getYear(
          end,
        ) > year || getYear(end) == year && getMonth(end) >= month)
      ) return true
      else return false
    }
    false
  }

  // TODO: These parameters are an insane dependency magnet
  //       Also the ID is not even used
  //       We need to refactor this and similar methods
  @nowarn("msg=unused explicit parameter")
  private def getSalaryChange(id: String, contract: Contract, date: Long): Signal[Option[SalaryChange]] =
    Signal.dynamic {
      val salaryChanges = jsImplicits.repositories.salaryChanges.all.value
      salaryChanges
        .map(_.signal.value)
        .filter(p => contract.contractAssociatedPaymentLevel.get.contains(p.paymentLevel.get.getOrElse("")))
        .filter(_.fromDate.get.getOrElse(0L) <= date)
        .sortWith(_.fromDate.get.getOrElse(0L) > _.fromDate.get.getOrElse(0L))
        .headOption
    }

  def getTotalHours(id: String, contract: Contract): Int = {
    contract.contractHoursPerMonth.get.getOrElse(0) * dateDiffMonth(
      contract.contractStartDate.get.getOrElse(0L),
      contract.contractEndDate.get.getOrElse(0L),
    )
  }

  def getMoneyPerHour(id: String, contract: Contract, date: Long): Signal[BigDecimal] =
    Signal.dynamic {
      getSalaryChange(id, contract, date).value
        .flatMap(_.value.get)
        .getOrElse(BigDecimal(0))
    }

  def getLimit(id: String, contract: Contract, date: Long): Signal[BigDecimal] =
    Signal.dynamic {
      getSalaryChange(id, contract, date).value
        .flatMap(_.limit.get)
        .getOrElse(BigDecimal(0))
    }

  def moneyPerHour =
    new UIReadOnlyAttribute[Contract, String](
      label = "€/h",
      getter = (id, contract) =>
        Signal { toMoneyString(getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L)).value) },
      readConverter = identity,
      formats = Seq(
        UIFormat(
          (id, contract) =>
            Signal {
              getMoneyPerHour(id, contract, contract.contractStartDate.get.getOrElse(0L)).value != getMoneyPerHour(
                id,
                contract,
                js.Date.now().toLong,
              ).value
            },
          "bg-red-200 text-red-600",
        ),
      ),
    )
}
