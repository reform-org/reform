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
import typings.reformOrgIdb.reformOrgIdbStrings.versionchange
import org.scalajs.dom.IDBVersionChangeEvent
import org.scalablytyped.runtime.StObject
import typings.reformOrgIdb.buildEntryMod.IDBPDatabaseExtends
import typings.std.ArrayLike
import typings.reformOrgIdb.buildEntryMod.IDBPObjectStoreExtends
import typings.reformOrgIdb.buildEntryMod.IDBPTransactionExtends
import typings.reformOrgIdb.buildEntryMod.IDBPIndex
import typings.reformOrgIdb.buildEntryMod.DBSchemaValue
import org.scalablytyped.runtime.StringDictionary

type Pick[T, K /* <: /* keyof T */ java.lang.String */ ] = T

type Exclude[T, U] = T

type Extract[T, U] = T

type DBSchema = StringDictionary[DBSchemaValue]

type KeyOf[T /* <: js.Object */ ] = Extract[ /* keyof T */ String, String]

type Omit[T, K] = Pick[T, Exclude[ /* keyof T */ String, K]]

type StoreKey[DBTypes /* <: DBSchema */, StoreName /* <: /* keyof DBTypes */ String */ ] =
  /* import warning: importer.ImportType#apply Failed type conversion: DBTypes[StoreName]['key'] */ js.Any

type StoreNames[DBTypes /* <: DBSchema */ ] = KeyOf[DBTypes]

type StoreValue[DBTypes /* <: DBSchema */, StoreName /* <: /* keyof DBTypes */ String */ ] =
  /* import warning: importer.ImportType#apply Failed type conversion: DBTypes[StoreName]['value'] */ js.Any

trait OpenDBCallbacks[DBTypes /* <: DBSchema */ ] extends StObject {}

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

@js.native
trait IDBPObjectStore[
    DBTypes /* <: DBSchema */,
    TxStores /* <: ArrayLike[StoreNames[DBTypes]] */,
    StoreName /* <: StoreNames[DBTypes] */,
    Mode, /* <: IDBTransactionMode */
] extends StObject
    with IDBPObjectStoreExtends {
  def get(query: StoreKey[DBTypes, StoreName]): js.Promise[js.UndefOr[StoreValue[DBTypes, StoreName]]] = js.native

  def put(
      value: StoreValue[DBTypes, StoreName],
      key: StoreKey[DBTypes, StoreName],
  ): js.Promise[StoreKey[DBTypes, StoreName]] = js.native

}

trait IDBPTransaction[
    DBTypes /* <: DBSchema */,
    TxStores /* <: ArrayLike[StoreNames[DBTypes]] */,
    Mode, /* <: IDBTransactionMode */
] extends StObject
    with IDBPTransactionExtends {

  val done: js.Promise[Unit]

  def objectStore[
      StoreName, /* <: /* import warning: importer.ImportType#apply Failed type conversion: TxStores[number] */ js.Any */
  ](name: StoreName): IDBPObjectStore[DBTypes, TxStores, StoreName, Mode]

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
