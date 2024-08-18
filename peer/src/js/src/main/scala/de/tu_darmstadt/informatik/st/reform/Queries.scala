package de.tu_darmstadt.informatik.st.reform

import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.entity.*
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.*
import de.tu_darmstadt.informatik.st.reform.repo.*
import rescala.default.*

import scala.scalajs.js

object Queries {

implicit class ContractQueries(contract: Contract)(using jsImplicits: JSImplicits) {

  val hiwi: Signal[Option[Synced[Hiwi]]] = Signal.dynamic {
    contract.contractAssociatedHiwi.option.flatMap(id =>
        jsImplicits.repositories.hiwis.find(id).value
    )
  }

  val paymentLevel: Signal[Option[Synced[PaymentLevel]]] = Signal.dynamic {
    contract.contractAssociatedPaymentLevel.option.flatMap(id =>
      jsImplicits.repositories.paymentLevels.find(id).value
    )
  }

  def salaryChanges(date: Long): Signal[Option[Synced[SalaryChange]]] = Signal.dynamic {
    paymentLevel.value.flatMap(paymentLevel =>
      jsImplicits.repositories.salaryChanges.filter(salaryChanges =>
        salaryChanges.signal.map(salaryChanges =>
          salaryChanges.paymentLevel == paymentLevel
          && salaryChanges.fromDate.hasValue
          && salaryChanges.fromDate.get <= date
        )
      )
      .value
      .sortBy(x => x.signal.value.fromDate.get)
      .headOption
    )
  }

  val project: Signal[Option[Synced[Project]]] = Signal.dynamic {
    contract.contractAssociatedProject.option.flatMap(id =>
      jsImplicits.repositories.projects.find(id).value
    )
  }

  def moneyPerHour(date: Long): Signal[BigDecimal] = Signal.dynamic {
    salaryChanges(date).value
      .flatMap(_.signal.value.value.option)
      .getOrElse(BigDecimal(0))
  }

  val totalHours: Int = {
    val hoursPerMonth = contract.contractHoursPerMonth.getOrElse(0)
    dateDiffMonth(
      contract.contractStartDate.getOrElse(0L),
      contract.contractEndDate.getOrElse(0L),
    ) * hoursPerMonth
  }

  def isActiveInMonth(month: Int, year: Int): Boolean = {
    if (contract.contractStartDate.option.isEmpty || contract.contractEndDate.option.isEmpty) {
      return false
    }

    val start = contract.contractStartDate.get
    val end = contract.contractEndDate.get

    getYear(end) == year && month <= getMonth(end)
      || getYear(start) == year && getMonth(start) <= month
      || getYear(start) < year && year < getYear(end)
  }

  def limit(date: Long): Signal[BigDecimal] = Signal.dynamic {
    salaryChanges(date).value
      .flatMap(_.signal.value.limit.option)
      .getOrElse(BigDecimal(0))
  }

  val documents: Signal[Seq[String]] = Signal.dynamic {
    contract.contractSchema.option
      .flatMap({ schema =>
        jsImplicits.repositories.contractSchemas
          .find(schema)
          .value
          .flatMap(_.signal.value.files.option)
      })
      .getOrElse(Seq.empty)
  }

  val salaryDidChange: Signal[Boolean] = Signal.dynamic {
    moneyPerHour(contract.contractStartDate.getOrElse(0L)).value != moneyPerHour(js.Date.now().toLong).value
  }

  val hourlyWage: Signal[BigDecimal] = Signal.dynamic {
    moneyPerHour(contract.contractStartDate.getOrElse(0L)).value
  }

  val monthlyCost: Signal[BigDecimal] = Signal.dynamic {
    val hoursPerMonth = contract.contractHoursPerMonth.getOrElse(0)
    hourlyWage.value * hoursPerMonth
  }
}

}
