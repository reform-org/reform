package webapp

import loci.registry.Registry
import webapp.entity.*
import webapp.npm.IIndexedDB
import webapp.repo.Repository

case class Repositories(
    val projects: Repository[Project],
    val users: Repository[User],
    val hiwis: Repository[Hiwi],
    val supervisors: Repository[Supervisor],
    val contractSchemas: Repository[ContractSchema],
    val paymentLevels: Repository[PaymentLevel],
    val salaryChanges: Repository[SalaryChange],
    val requiredDocuments: Repository[RequiredDocument],
    val contracts: Repository[Contract]
) {}

object Repositories {
  def apply()(using registry: Registry, indexedDb: IIndexedDB): Repositories = this(
    Repository("project", Project.empty),
    Repository("user", User.empty),
    Repository("hiwi", Hiwi.empty),
    Repository("supervisor", Supervisor.empty),
    Repository("contract-schema", ContractSchema.empty),
    Repository("payment-level", PaymentLevel.empty),
    Repository("salary-change", SalaryChange.empty),
    Repository("required-document", RequiredDocument.empty),
    Repository("contracts", Contract.empty)
  )
}
