package webapp.repo

import webapp.npm.*

import scala.concurrent.Future
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec

case class Repository[T](private val name: String, private val defaultValue: T)(using codec: JsonValueCodec[T]) {

  def getOrDefault(id: String): Future[T] =
    IndexedDB
      .get(getKey(id))
      .map(option => option.getOrElse(defaultValue))

  private def getKey(id: String): String = s"$name-$id"

  def set(id: String, value: T): Future[Unit] =
    IndexedDB.set(getKey(id), value)

}
