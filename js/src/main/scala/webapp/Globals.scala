package webapp

import scala.concurrent.ExecutionContext

// macrotask executor breaks indexeddb
given ExecutionContext = scala.concurrent.ExecutionContext.global

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

object Globals {
  val VITE_SELENIUM: Boolean = js.`import`.meta.env.VITE_SELENIUM.asInstanceOf[String] == "true"

  val VITE_DATABASE_VERSION: String = js.`import`.meta.env.VITE_DATABASE_VERSION.asInstanceOf[String]

  val VITE_PROTOCOL_VERSION: String = js.`import`.meta.env.VITE_PROTOCOL_VERSION.asInstanceOf[String]

  val VITE_SERVER_PROTOCOL: String = js.`import`.meta.env.VITE_SERVER_PROTOCOL.asInstanceOf[String]

  val VITE_SERVER_HOST: String = js.`import`.meta.env.VITE_SERVER_HOST.asInstanceOf[String]

  val VITE_SERVER_PORT: String = js.`import`.meta.env.VITE_SERVER_PORT.asInstanceOf[String]

  val VITE_DISCOVERY_SERVER_PROTOCOL: String = js.`import`.meta.env.VITE_DISCOVERY_SERVER_PROTOCOL.asInstanceOf[String]

  val VITE_DISCOVERY_SERVER_HOST: String = js.`import`.meta.env.VITE_DISCOVERY_SERVER_HOST.asInstanceOf[String]

  val VITE_DISCOVERY_SERVER_PORT: String = js.`import`.meta.env.VITE_DISCOVERY_SERVER_PORT.asInstanceOf[String]

  val VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL.asInstanceOf[String]

  val VITE_DISCOVERY_SERVER_WEBSOCKET_HOST: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_HOST.asInstanceOf[String]

  val VITE_DISCOVERY_SERVER_WEBSOCKET_PORT: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_PORT.asInstanceOf[String]

  val VITE_TURN_SERVER_HOST: String = js.`import`.meta.env.VITE_TURN_SERVER_HOST.asInstanceOf[String]

  val VITE_TURN_SERVER_PORT: String = js.`import`.meta.env.VITE_TURN_SERVER_PORT.asInstanceOf[String]

  val VITE_ALWAYS_ONLINE_PEER_PROTOCOL: String =
    js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_PROTOCOL.asInstanceOf[String]

  val VITE_ALWAYS_ONLINE_PEER_HOST: String = js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_HOST.asInstanceOf[String]

  val VITE_ALWAYS_ONLINE_PEER_PORT: String = js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_PORT.asInstanceOf[String]
}
