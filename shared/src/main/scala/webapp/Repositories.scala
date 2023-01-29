package webapp

import webapp.entity.ContractSchema
import webapp.entity.Hiwi
import webapp.entity.PaymentLevel
import webapp.entity.Project
import webapp.entity.SalaryChange
import webapp.entity.Supervisor
import webapp.entity.User
import webapp.npm.IIndexedDB
import webapp.repo.Repository
import webapp.webrtc.WebRTCService

class Repositories(using webrtc: WebRTCService, indexedDb: IIndexedDB) {

  val projects: Repository[Project] = Repository("project", Project.empty)
  val users: Repository[User] = Repository("user", User.empty)
  val hiwis: Repository[Hiwi] = Repository("hiwi", Hiwi.empty)
  val supervisor: Repository[Supervisor] = Repository("supervisor", Supervisor.empty)
  val contractSchemas: Repository[ContractSchema] = Repository("contractSchema", ContractSchema.empty)
  val paymentLevels: Repository[PaymentLevel] = Repository("paymentLevels", PaymentLevel.empty)
  val salaryChanges: Repository[SalaryChange] = Repository("salaryChanges", SalaryChange.empty)
}
