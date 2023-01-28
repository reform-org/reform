package webapp

import webapp.entity.{ContractSchema, Hiwi, PaymentLevel, Project, SalaryChange, Supervisor, User}
import webapp.repo.Repository
import loci.registry.Registry
import webapp.npm.IIndexedDB

class Repositories(using registry: Registry, indexedDb: IIndexedDB) {

  val projects: Repository[Project] = Repository("project", Project.empty)
  val users: Repository[User] = Repository("user", User.empty)
  val hiwis: Repository[Hiwi] = Repository("hiwi", Hiwi.empty)
  val supervisor: Repository[Supervisor] = Repository("supervisor", Supervisor.empty)
  val contractSchemas: Repository[ContractSchema] = Repository("contractSchema", ContractSchema.empty)
  val paymentLevels: Repository[PaymentLevel] = Repository("paymentLevels", PaymentLevel.empty)
  val salaryChanges: Repository[SalaryChange] = Repository("salaryChanges", SalaryChange.empty)
}
