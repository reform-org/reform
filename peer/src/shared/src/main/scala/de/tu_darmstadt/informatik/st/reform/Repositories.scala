package de.tu_darmstadt.informatik.st.reform

import de.tu_darmstadt.informatik.st.reform.entity.Document
import de.tu_darmstadt.informatik.st.reform.entity.*
import de.tu_darmstadt.informatik.st.reform.npm.IIndexedDB
import de.tu_darmstadt.informatik.st.reform.repo.Repository
import de.tu_darmstadt.informatik.st.reform.webrtc.PingService
import loci.registry.Registry

case class Repositories(
    projects: Repository[Project],
    users: Repository[User],
    hiwis: Repository[Hiwi],
    supervisors: Repository[Supervisor],
    contractSchemas: Repository[ContractSchema],
    paymentLevels: Repository[PaymentLevel],
    salaryChanges: Repository[SalaryChange],
    documents: Repository[Document],
    contracts: Repository[Contract],
)(using registry: Registry) {
  val _ = PingService()
}

object Repositories {
  def apply()(using registry: Registry, indexedDb: IIndexedDB): Repositories = this(
    Repository("project", Project.empty),
    Repository("user", User.empty),
    Repository("hiwi", Hiwi.empty),
    Repository("supervisor", Supervisor.empty),
    Repository("contract-schema", ContractSchema.empty),
    Repository("payment-level", PaymentLevel.empty),
    Repository("salary-change", SalaryChange.empty),
    Repository("required-document", Document.empty),
    Repository("contracts", Contract.empty),
  )
}
