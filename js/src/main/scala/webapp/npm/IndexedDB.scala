package webapp.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

import webapp.given_ExecutionContext
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import typings.std.IDBTransactionMode
import scala.annotation.nowarn
import scala.scalajs.js.annotation.JSImport
import typings.reformOrgIdb.buildEntryMod.IDBPTransaction
import typings.reformOrgIdb.reformOrgIdbStrings.versionchange
import org.scalajs.dom.IDBVersionChangeEvent
import org.scalablytyped.runtime.StObject
import typings.reformOrgIdb.buildEntryMod.IDBPDatabaseExtends
import typings.reformOrgIdb.buildEntryMod.IDBPObjectStore
import typings.std.ArrayLike

trait OpenDBCallbacks[DBTypes /* <: DBSchema */ ] extends StObject {

  /** Called if there are older versions of the database open on the origin, so this version cannot open.
    *
    * @param currentVersion
    *   Version of the database that's blocking this one.
    * @param blockedVersion
    *   The version of the database being blocked (whatever version you provided to `openDB`).
    * @param event
    *   The event object for the associated `blocked` event.
    */
  var blocked: js.UndefOr[
    js.Function3[
      /* currentVersion */ Double,
      /* blockedVersion */ Double | Null,
      /* event */ IDBVersionChangeEvent,
      Unit,
    ],
  ] = js.undefined

  /** Called if this connection is blocking a future version of the database from opening.
    *
    * @param currentVersion
    *   Version of the open database (whatever version you provided to `openDB`).
    * @param blockedVersion
    *   The version of the database that's being blocked.
    * @param event
    *   The event object for the associated `versionchange` event.
    */
  var blocking: js.UndefOr[
    js.Function3[
      /* currentVersion */ Double,
      /* blockedVersion */ Double | Null,
      /* event */ IDBVersionChangeEvent,
      Unit,
    ],
  ] = js.undefined

  /** Called if the browser abnormally terminates the connection. This is not called when `db.close()` is called.
    */
  var terminated: js.UndefOr[js.Function0[Unit]] = js.undefined

  /** Called if this version of the database has never been opened before. Use it to specify the schema for the
    * database.
    *
    * @param database
    *   A database instance that you can use to add/remove stores and indexes.
    * @param oldVersion
    *   Last version of the database opened by the user.
    * @param newVersion
    *   Whatever new version you provided.
    * @param transaction
    *   The transaction for this upgrade. This is useful if you need to get data from other stores as part of a
    *   migration.
    * @param event
    *   The event object for the associated 'upgradeneeded' event.
    */
  var upgrade: js.UndefOr[
    js.Function5[
      /* database */ IDBPDatabase[DBTypes],
      /* oldVersion */ Double,
      /* newVersion */ Double | Null,
      /* transaction */ IDBPTransaction[DBTypes, js.Array[js.Any], versionchange],
      /* event */ IDBVersionChangeEvent,
      Unit,
    ],
  ] = js.undefined
}

object OpenDBCallbacks {

  inline def apply[DBTypes /* <: DBSchema */ ](): OpenDBCallbacks[DBTypes] = {
    val __obj = js.Dynamic.literal()
    __obj.asInstanceOf[OpenDBCallbacks[DBTypes]]
  }

  @scala.inline
  implicit open class MutableBuilder[Self <: OpenDBCallbacks[?], DBTypes /* <: DBSchema */ ](
      val x: Self & OpenDBCallbacks[DBTypes],
  ) extends AnyVal {

    inline def setUpgrade(
        value: (
            /* database */ IDBPDatabase[DBTypes], /* oldVersion */ Double, /* newVersion */ Double | Null,
            /* transaction */ IDBPTransaction[
              DBTypes,
              js.Array[js.Any],
              versionchange,
            ], /* event */ IDBVersionChangeEvent,
        ) => Unit,
    ): Self = StObject.set(x, "upgrade", js.Any.fromFunction5(value))

  }
}

object mod {

  @JSImport("@reform-org/idb", JSImport.Namespace)
  @js.native
  val ^ : js.Any = js.native

  inline def openDB[DBTypes /* <: DBSchema */ ](
      name: String,
      version: Double,
      param2: OpenDBCallbacks[DBTypes],
  ): js.Promise[IDBPDatabase[DBTypes]] = (^.asInstanceOf[js.Dynamic]
    .applyDynamic("openDB")(name.asInstanceOf[js.Any], version.asInstanceOf[js.Any], param2.asInstanceOf[js.Any]))
    .asInstanceOf[js.Promise[IDBPDatabase[DBTypes]]]
}

@js.native
trait IDBPDatabase[DBTypes /* <: DBSchema */ ] extends StObject with IDBPDatabaseExtends {

  def createObjectStore[Name /* <: StoreNames[DBTypes] */ ](
      name: Name,
  ): IDBPObjectStore[DBTypes, ArrayLike[js.Any], Name, versionchange] = js.native

  def transaction[Names /* <: ArrayLike[StoreNames[DBTypes]] */, Mode /* <: IDBTransactionMode */ ](
      storeNames: Names,
      mode: Mode,
  ): IDBPTransaction[DBTypes, Names, Mode] = js.native

}

class IndexedDB extends IIndexedDB {

  val database =
    mod
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
