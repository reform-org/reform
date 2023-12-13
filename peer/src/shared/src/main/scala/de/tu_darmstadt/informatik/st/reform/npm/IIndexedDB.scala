package de.tu_darmstadt.informatik.st.reform.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future

trait IIndexedDB {

  def requestPersistentStorage(): Unit

  def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]]

  def update[T](key: String, scalaFun: Option[T] => T)(using codec: JsonValueCodec[T]): Future[T]
}
