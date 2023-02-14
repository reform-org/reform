package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import webapp.npm.*

import webapp.given_ExecutionContext
import scala.concurrent.Future

case class Storage[T](private val name: String, private val defaultValue: T)(using
    codec: JsonValueCodec[T],
    indexedDb: IIndexedDB,
) {

  def getOrDefault(id: String): Future[T] =
    indexedDb
      .get[T](getKey(id))
      .map(option => option.getOrElse(defaultValue))

  private def getKey(id: String): String = s"$name-$id"

  def update(id: String, fun: Option[T] => T): Future[T] = {
    indexedDb.update(getKey(id), fun)
  }
}
