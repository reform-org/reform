package webapp.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

import concurrent.ExecutionContext.Implicits.global
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import typings.reformOrgIdb.buildEntryMod.OpenDBCallbacks
import typings.std.IDBTransactionMode
import scala.annotation.nowarn

class IndexedDB extends IIndexedDB {

  val database =
    typings.reformOrgIdb.mod
      .openDB(
        "reform",
        2,
        OpenDBCallbacks()
          .setUpgrade((db, _, _, _, _) => {
            val _ = db.createObjectStore("reform")
          }),
      )
      .toFuture

  override def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]] = {
    for db <- database
    tx = db.transaction(js.Array("reform"), IDBTransactionMode.readonly)
    store = tx.objectStore("reform")
    v <- store.get(key).toFuture
    _ <- tx.done.toFuture
    value = Option(v.orNull).map(castFromJsDynamic(_))
    yield {
      value
    }
  }

  @nowarn("msg=unused implicit parameter")
  given optionCodec[T](using codec: JsonValueCodec[T]): JsonValueCodec[Option[T]] = JsonCodecMaker.make

  override def update[T](key: String, scalaFun: Option[T] => T)(using codec: JsonValueCodec[T]): Future[T] = {
    for db <- database
    tx = db.transaction(js.Array("reform"), IDBTransactionMode.readwrite)
    store = tx.objectStore("reform")
    _ = println(store)
    v <- store.get(key).toFuture
    value = Option(v.orNull).map(castFromJsDynamic(_))
    newValue = scalaFun(value)
    _ <- store.put(castToJsDynamic(newValue), key).toFuture
    _ <- tx.done.toFuture
    yield {
      newValue
    }
  }

  private def castToJsDynamic[T](value: T)(using codec: JsonValueCodec[T]): js.Any =
    JSON.parse(writeToString(value))

  private def castFromJsDynamic[T](dynamic: js.Any)(using codec: JsonValueCodec[T]) =
    readFromString(JSON.stringify(dynamic))
}
