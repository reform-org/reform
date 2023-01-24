package webapp

import webapp.entity.{Hiwi, PaymentLevel, Project, SalaryChange, User}
import webapp.repo.Repository

object Repositories {

  val projects: Repository[Project] = Repository("project", Project.empty)
  val users: Repository[User] = Repository("user", User.empty)
  val hiwis: Repository[Hiwi] = Repository("hiwi", Hiwi.empty)
  val paymentLevels: Repository[PaymentLevel] = Repository("paymentLevels", PaymentLevel.empty)
  val salaryChanges: Repository[SalaryChange] = Repository("salaryChanges", SalaryChange.empty)
}
