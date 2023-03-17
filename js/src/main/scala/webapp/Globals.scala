package webapp

import scala.concurrent.ExecutionContext

// macrotask executor breaks indexeddb
given ExecutionContext = scala.concurrent.ExecutionContext.global

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport
import js.UndefOr
import webapp.services.Toaster
import webapp.services.MailService
import webapp.services.RoutingService
import webapp.npm.IIndexedDB
import loci.registry.Registry
import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService

abstract case class JSImplicits() {
  lazy val toaster: Toaster
  lazy val mailing: MailService
  lazy val routing: RoutingService
  lazy val indexeddb: IIndexedDB
  lazy val registry: Registry
  lazy val webrtc: WebRTCService
  lazy val repositories: Repositories
  lazy val discovery: DiscoveryService
}

object Globals {
  lazy val VITE_SELENIUM: Boolean = js.`import`.meta.env.VITE_SELENIUM.asInstanceOf[String] == "true"

  lazy val VITE_DATABASE_VERSION: String = js.`import`.meta.env.VITE_DATABASE_VERSION.asInstanceOf[String]

  lazy val VITE_PROTOCOL_VERSION: String = js.`import`.meta.env
    .asInstanceOf[UndefOr[js.Dynamic]]
    .map(_.VITE_PROTOCOL_VERSION.asInstanceOf[String])
    .getOrElse("test")

  lazy val VITE_SERVER_PROTOCOL: String = js.`import`.meta.env.VITE_SERVER_PROTOCOL.asInstanceOf[String]

  lazy val VITE_SERVER_HOST: String = js.`import`.meta.env.VITE_SERVER_HOST.asInstanceOf[String]

  lazy val VITE_SERVER_PATH: String = js.`import`.meta.env.VITE_SERVER_PATH.asInstanceOf[String]

  lazy val VITE_SERVER_PORT: String = js.`import`.meta.env.VITE_SERVER_PORT.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_PROTOCOL: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_PROTOCOL.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_HOST: String = js.`import`.meta.env.VITE_DISCOVERY_SERVER_HOST.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_PATH: String = js.`import`.meta.env.VITE_DISCOVERY_SERVER_PATH.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_LISTEN_PORT: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_LISTEN_PORT.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_PUBLIC_PORT: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_PUBLIC_PORT.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_HOST: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_HOST.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PATH: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_PATH.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT.asInstanceOf[String]

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PUBLIC_PORT: String =
    js.`import`.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_PUBLIC_PORT.asInstanceOf[String]

  lazy val VITE_TURN_SERVER_HOST: String = js.`import`.meta.env.VITE_TURN_SERVER_HOST.asInstanceOf[String]

  lazy val VITE_TURN_SERVER_PORT: String = js.`import`.meta.env.VITE_TURN_SERVER_PORT.asInstanceOf[String]

  lazy val VITE_ALWAYS_ONLINE_PEER_PROTOCOL: String =
    js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_PROTOCOL.asInstanceOf[String]

  lazy val VITE_ALWAYS_ONLINE_PEER_HOST: String = js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_HOST.asInstanceOf[String]

  lazy val VITE_ALWAYS_ONLINE_PEER_PATH: String = js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_PATH.asInstanceOf[String]

  lazy val VITE_ALWAYS_ONLINE_PEER_SUBPROTOCOL: String =
    js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_SUBPROTOCOL.asInstanceOf[String]

  lazy val VITE_ALWAYS_ONLINE_PEER_PUBLIC_PORT: String =
    js.`import`.meta.env.VITE_ALWAYS_ONLINE_PEER_PUBLIC_PORT.asInstanceOf[String]

  lazy val VITE_DEKANAT_MAIL: String =
    js.`import`.meta.env.VITE_DEKANAT_MAIL.asInstanceOf[String]

  lazy val VITE_CONTRACT_PDF_PATH: String =
    js.`import`.meta.env.VITE_CONTRACT_PDF_PATH.asInstanceOf[String]

  lazy val VITE_LETTER_PDF_PATH: String =
    js.`import`.meta.env.VITE_LETTER_PDF_PATH.asInstanceOf[String]

  lazy val APP_VERSION: String = js.Dynamic.global.APP_VERSION.asInstanceOf[String]
}
