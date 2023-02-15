package webapp

import loci.registry.Registry
import webapp.entity.*
import webapp.npm.IIndexedDB
import webapp.repo.Repository
import webapp.webrtc.PingService

class Repositories(using registry: Registry, indexedDb: IIndexedDB) {

  val _ = PingService(using registry)

  val projects: Repository[Project] = Repository("project", Project.empty)
  val users: Repository[User] = Repository("user", User.empty)
  val hiwis: Repository[Hiwi] = Repository("hiwi", Hiwi.empty)
  val supervisors: Repository[Supervisor] = Repository("supervisor", Supervisor.empty)
  val contractSchemas: Repository[ContractSchema] = Repository("contract-schema", ContractSchema.empty)
  val paymentLevels: Repository[PaymentLevel] = Repository("payment-level", PaymentLevel.empty)
  val salaryChanges: Repository[SalaryChange] = Repository("salary-change", SalaryChange.empty)
  val requiredDocuments: Repository[RequiredDocument] = Repository("required-document", RequiredDocument.empty)
  val contracts: Repository[Contract] = Repository("contracts", Contract.empty)
}
