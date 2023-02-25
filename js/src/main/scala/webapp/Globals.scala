package webapp

import scala.concurrent.ExecutionContext

// macrotask executor breaks indexeddb
given ExecutionContext = scala.concurrent.ExecutionContext.global

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../../src/main/scala/webapp/globals.js", JSImport.Namespace)
object Globals extends js.Object {

  val isSelenium: Boolean = js.native
  val discoveryServerURL: String = js.native
  val discoveryServerWebsocketURL: String = js.native
  val turnServerURL: String = js.native
  val alwaysOnlinePeerURL: String = js.native
}
