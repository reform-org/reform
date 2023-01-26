package webapp

import webapp.entity.{ContractSchema, Hiwi, PaymentLevel, Project, SalaryChange, Supervisor, User}
import webapp.repo.Repository
import loci.registry.Registry

object Repositories {
  // This is in the same class so the repositories are loaded as soon as you are connected.
  // Otherwise background sync may not work properly.
  val lociRegistry: Registry = new Registry

  val projects: Repository[Project] = Repository("project", Project.empty)
  val users: Repository[User] = Repository("user", User.empty)
  val hiwis: Repository[Hiwi] = Repository("hiwi", Hiwi.empty)
  val supervisor: Repository[Supervisor] = Repository("supervisor", Supervisor.empty)
  val contractSchemas: Repository[ContractSchema] = Repository("contractSchema", ContractSchema.empty)
  val paymentLevels: Repository[PaymentLevel] = Repository("paymentLevels", PaymentLevel.empty)
  val salaryChanges: Repository[SalaryChange] = Repository("salaryChanges", SalaryChange.empty)
}
