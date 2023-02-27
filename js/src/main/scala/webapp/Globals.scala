package webapp

import scala.concurrent.ExecutionContext

// macrotask executor breaks indexeddb
given ExecutionContext = scala.concurrent.ExecutionContext.global

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

object Globals2 {
  // TODO FIXME convert to this
  val VITE_SELENIUM: Boolean = js.`import`.meta.env.VITE_SELENIUM.asInstanceOf[Boolean]
}

@js.native
@JSImport("../../src/main/scala/webapp/globals.js", JSImport.Namespace)
object Globals extends js.Object {

  val VITE_SELENIUM: Boolean = js.native

  val VITE_SERVER_PROTOCOL: String = js.native
  val VITE_SERVER_HOST: String = js.native
  val VITE_SERVER_PORT: String = js.native

  val VITE_DISCOVERY_SERVER_PROTOCOL: String = js.native
  val VITE_DISCOVERY_SERVER_HOST: String = js.native
  val VITE_DISCOVERY_SERVER_PORT: String = js.native

  val VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL: String = js.native
  val VITE_DISCOVERY_SERVER_WEBSOCKET_HOST: String = js.native
  val VITE_DISCOVERY_SERVER_WEBSOCKET_PORT: String = js.native

  val VITE_TURN_SERVER_HOST: String = js.native
  val VITE_TURN_SERVER_PORT: String = js.native

  val VITE_ALWAYS_ONLINE_PEER_PROTOCOL: String = js.native
  val VITE_ALWAYS_ONLINE_PEER_HOST: String = js.native
  val VITE_ALWAYS_ONLINE_PEER_PORT: String = js.native
}
