package webapp.repo

import webapp.GrowOnlySet

import scala.concurrent.Future
import webapp.npm.IIndexedDB

// TODO: Might use normal sets
case class IdSetStorage(name: String)(using indexedDb: IIndexedDB) {

  private val storage: Storage[GrowOnlySet[String]] = Storage(name, GrowOnlySet.empty)

  def get: Future[GrowOnlySet[String]] = storage.getOrDefault("ids")

  def set(value: GrowOnlySet[String]): Future[Unit] = storage.set("ids", value)
}
