package webapp.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future

trait IIndexedDB {

  def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]]

  def update[T](key: String, scalaFun: Option[T] => T)(using codec: JsonValueCodec[T], codec2: JsonValueCodec[Option[T]]): Future[T]
}
