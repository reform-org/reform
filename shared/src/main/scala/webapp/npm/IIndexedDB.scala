package webapp.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future
import scala.scalajs.js

trait IIndexedDB {

  def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]]

  def set[T](key: String, value: T)(using codec: JsonValueCodec[T]): Future[Unit]
}
