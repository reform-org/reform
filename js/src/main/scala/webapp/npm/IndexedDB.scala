package webapp.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.{JSON, Promise}
import scala.scalajs.js.annotation.JSImport
import concurrent.ExecutionContext.Implicits.global

object IndexedDB extends IIndexedDB {

  override def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]] = {
    val promise: js.Promise[js.UndefOr[js.Dynamic]] = NativeImpl.get(key)
    promise.toFuture
      .map(undefOr =>
        undefOr.toOption
          .map(dynamic => castFromJsDynamic(dynamic)),
      )
  }

  override def set[T](key: String, value: T)(using codec: JsonValueCodec[T]): Future[Unit] = {
    val dynamic = castToJsDynamic(value)
    val promise: Promise[Unit] = NativeImpl.set(key, dynamic)
    promise.toFuture
  }

  private def castToJsDynamic[T](value: T)(using codec: JsonValueCodec[T]): js.Dynamic =
    JSON.parse(writeToString(value))

  private def castFromJsDynamic[T](dynamic: js.Dynamic)(using codec: JsonValueCodec[T]) =
    readFromString(JSON.stringify(dynamic))

  // https://github.com/jakearchibald/idb-keyval/blob/main/src/index.ts#L44
  @js.native
  @JSImport("idb-keyval", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def get(key: String): js.Promise[js.UndefOr[js.Dynamic]] = js.native

    def set(key: String, value: js.Dynamic): js.Promise[Unit] = js.native
  }
}
