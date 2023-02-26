package webapp.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

import webapp.given_ExecutionContext
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import scala.annotation.nowarn
import scala.scalajs.js.annotation.JSImport
import org.scalajs.dom.IDBVersionChangeEvent

import org.scalajs.dom.window
import webapp.utils.Futures.*
import webapp.services.Toaster
import webapp.services.ToastMode
import webapp.services.ToastType
import webapp.Globals

// manually extracted from scalablytyped

trait StObject extends js.Object

object StObject {
  @inline
  def set[Self <: js.Any](x: Self, key: String, value: Any): Self = {
    x.asInstanceOf[js.Dynamic].updateDynamic(key)(value.asInstanceOf[js.Any])
    x
  }
}

type Pick[T, K /* <: /* keyof T */ java.lang.String */ ] = T

type Exclude[T, U] = T

type Extract[T, U] = T

type KeyOf[T /* <: js.Object */ ] = Extract[ /* keyof T */ String, String]

type Omit[T, K] = Pick[T, Exclude[ /* keyof T */ String, K]]

type StoreKey[DBTypes /* <: DBSchema */, StoreName /* <: /* keyof DBTypes */ String */ ] =
  /* import warning: importer.ImportType#apply Failed type conversion: DBTypes[StoreName]['key'] */ js.Any

type StoreNames[DBTypes /* <: DBSchema */ ] = KeyOf[DBTypes]

type StoreValue[DBTypes /* <: DBSchema */, StoreName /* <: /* keyof DBTypes */ String */ ] =
  /* import warning: importer.ImportType#apply Failed type conversion: DBTypes[StoreName]['value'] */ js.Any

@js.native
sealed trait versionchange extends StObject
inline def versionchange: versionchange = "versionchange".asInstanceOf[versionchange]

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

trait IDBPDatabaseExtends extends StObject {}

object IDBPDatabaseExtends {}

@js.native
trait IDBPDatabase[DBTypes /* <: DBSchema */ ] extends StObject with IDBPDatabaseExtends {

  def createObjectStore[Name /* <: StoreNames[DBTypes] */ ](
      name: Name,
  ): IDBPObjectStore[DBTypes, Array[js.Any], Name, versionchange] = js.native

  def transaction[Names /* <: ArrayLike[StoreNames[DBTypes]] */, Mode /* <: IDBTransactionMode */ ](
      storeNames: Names,
      mode: Mode,
  ): IDBPTransaction[DBTypes, Names, Mode] = js.native
}

trait IDBPObjectStoreExtends extends StObject {}

object IDBPObjectStoreExtends {}

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

trait IDBPTransactionExtends extends StObject {}

object IDBPTransactionExtends {}

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

object stdStrings {

  @js.native
  sealed trait readonly extends StObject with IDBTransactionMode
  inline def readonly: readonly = "readonly".asInstanceOf[readonly]

  @js.native
  sealed trait readwrite extends StObject with IDBTransactionMode
  inline def readwrite: readwrite = "readwrite".asInstanceOf[readwrite]

  @js.native
  sealed trait versionchange extends StObject with IDBTransactionMode
  inline def versionchange: versionchange = "versionchange".asInstanceOf[versionchange]

}

trait IDBTransactionMode extends StObject
object IDBTransactionMode {

  inline def readonly: stdStrings.readonly = "readonly".asInstanceOf[stdStrings.readonly]

  inline def readwrite: stdStrings.readwrite = "readwrite".asInstanceOf[stdStrings.readwrite]

  inline def versionchange: stdStrings.versionchange =
    "versionchange".asInstanceOf[stdStrings.versionchange]
}

class IndexedDB(using toaster: Toaster) extends IIndexedDB {

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

  var requestedPersistentStorage = Globals.VITE_SELENIUM

  def requestPersistentStorage: Unit = {
    if (!requestedPersistentStorage) {
      println("request persistent storage")
      requestedPersistentStorage = true;
      if (
        !(!(js.Dynamic.global.navigator.storage))
          .asInstanceOf[Boolean] && (!(!js.Dynamic.global.navigator.storage.persist)).asInstanceOf[Boolean]
      ) {
        window.navigator.storage
          .persist()
          .toFuture
          .map(result => {
            if (result) {
              println("Your data will be safely stored in your browser. Please don't delete site data.")
            } else {
              println(
                "No persistent storage! Your data may get lost. Please allow the permission if the browser asks you.",
              )
            }
          })
          .toastOnError()
      } else {
        println(
          "No persistent storage API available! Your data can't be safely stored in your browser. Maybe you access this page over an insecure connection?",
        )
      }
    }
  }

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
