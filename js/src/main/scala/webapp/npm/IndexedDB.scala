package webapp.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

import concurrent.ExecutionContext.Implicits.global
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import typings.idb.buildEntryMod.OpenDBCallbacks
import typings.std.IDBTransactionMode
import typings.idb.buildEntryMod.StoreValue

class IndexedDB extends IIndexedDB {

  val database = 
    typings.idb.mod.openDB("reform", 1, OpenDBCallbacks()
    .setUpgrade((db, oldVersion, newVersion, transaction, event) => {
      db.createObjectStore("reform")
    })
    ).toFuture

  override def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]] = {
    val promise: js.Promise[js.UndefOr[js.Dynamic]] = NativeImpl.get(key)
    promise.toFuture
      .map(undefOr =>
        undefOr.toOption
          .map(dynamic => castFromJsDynamic(dynamic)),
      )
  }

  given optionCodec[T](using codec: JsonValueCodec[T]): JsonValueCodec[Option[T]] = JsonCodecMaker.make

  override def update[T](key: String, scalaFun: Option[T] => T)(using codec: JsonValueCodec[T]): Future[T] = {
    for
      db <- database
      tx = db.transaction(List("reform"), IDBTransactionMode.readwrite)
      store = tx.objectStore("reform")
      v <- store.get(key).toFuture
    do {
      val value = Option(v.orNull).map(_.asInstanceOf[T])
      val newValue = scalaFun(value)
      store.add(StoreValue(newValue), key)
    }
    
    val theFun: Function[js.Dynamic, js.Dynamic] = a => {
      val in = castFromJsDynamic[Option[T]](a)
      val value = scalaFun(in)
      castToJsDynamic(value)
    }
    val promise = NativeImpl.update(key, theFun)
    promise.toFuture.map(castFromJsDynamic)
  }

  private def castToJsDynamic[T](value: T)(using codec: JsonValueCodec[T]): js.Dynamic =
    JSON.parse(writeToString(value))

  private def castFromJsDynamic[T](dynamic: js.Dynamic)(using codec: JsonValueCodec[T]) =
    readFromString(JSON.stringify(dynamic))
}

// https://github.com/jakearchibald/idb-keyval/blob/main/src/index.ts#L44
@js.native
@JSImport("idb-keyval", JSImport.Namespace)
private object NativeImpl extends js.Object {

  def get(key: String): js.Promise[js.UndefOr[js.Dynamic]] = js.native

  def update(key: String, value: js.Function1[js.Dynamic, js.Dynamic]): js.Promise[Unit] = js.native
}
