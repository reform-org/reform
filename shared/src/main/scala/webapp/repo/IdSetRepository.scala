package webapp.repo

import webapp.GrowOnlySet

import scala.concurrent.Future

// TODO: Might use normal sets
case class IdSetRepository(name: String) {

  private val repo: Repository[GrowOnlySet[String]] = Repository(name, GrowOnlySet.empty)

  def get: Future[GrowOnlySet[String]] = repo.getOrDefault("ids")

  def set(value: GrowOnlySet[String]): Future[Unit] = repo.set("ids", value)
}
