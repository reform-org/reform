package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import webapp.npm.*

import webapp.given_ExecutionContext
import scala.concurrent.Future

case class Storage[T](private val name: String)(using
    codec: JsonValueCodec[T],
    indexedDb: IIndexedDB,
) {

  def getOrDefault(id: String, default: T): Future[T] = {
    indexedDb.update[T](
      getKey(id),
      currentValue => {
        currentValue match {
          case Some(v) => v
          case None    => default
        }
      },
    )
  }

  private def getKey(id: String): String = s"$name-$id"

  def update(id: String, fun: Option[T] => T): Future[T] = {
    indexedDb.update(getKey(id), fun)
  }
}
