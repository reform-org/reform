package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import webapp.npm.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Storage[T](private val name: String, private val defaultValue: T)(using
    codec: JsonValueCodec[T],
    indexedDb: IIndexedDB,
) {

  def getOrDefault(id: String): Future[T] =
    indexedDb
      .get(getKey(id))
      .map(option => option.getOrElse(defaultValue))

  private def getKey(id: String): String = s"$name-$id"

  def set(id: String, value: T): Future[Unit] =
    indexedDb.set(getKey(id), value)

}
