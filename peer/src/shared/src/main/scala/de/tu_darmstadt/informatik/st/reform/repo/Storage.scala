package de.tu_darmstadt.informatik.st.reform.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import de.tu_darmstadt.informatik.st.reform.npm.*

import scala.concurrent.Future

case class Storage[T](private val name: String)(using
    codec: JsonValueCodec[T],
    indexedDb: IIndexedDB,
) {

  def getOrDefault(id: String, default: T): Future[T] = {
    indexedDb.update[T](
      getKey(id),
      {
        case Some(v) => v
        case None    => default
      },
    )
  }

  private def getKey(id: String): String = s"$name-$id"

  def update(id: String, fun: Option[T] => T): Future[T] = {
    indexedDb.update(getKey(id), fun)
  }
}
