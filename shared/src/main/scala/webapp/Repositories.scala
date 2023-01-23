package webapp

import webapp.entity.{PaymentLevel, Project, User}
import webapp.repo.Repository

object Repositories {

  val projects: Repository[Project] = Repository("project", Project.empty)
  val users: Repository[User] = Repository("user", User.empty)
  val paymentLevels: Repository[PaymentLevel] = Repository("paymentLevels", PaymentLevel.empty)
}
