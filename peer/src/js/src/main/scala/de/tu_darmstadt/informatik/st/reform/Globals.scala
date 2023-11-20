package de.tu_darmstadt.informatik.st.reform

import scala.concurrent.ExecutionContext

// macrotask executor breaks indexeddb
given ExecutionContext = scala.concurrent.ExecutionContext.global

import scala.scalajs.js
import js.UndefOr
import de.tu_darmstadt.informatik.st.reform.services.Toaster
import de.tu_darmstadt.informatik.st.reform.services.MailService
import de.tu_darmstadt.informatik.st.reform.services.RoutingService
import de.tu_darmstadt.informatik.st.reform.npm.IIndexedDB
import loci.registry.Registry
import de.tu_darmstadt.informatik.st.reform.webrtc.WebRTCService
import de.tu_darmstadt.informatik.st.reform.services.DiscoveryService

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

object Env {
  def get(name: String): String = {
    val opt = js.`import`.meta.env.selectDynamic(name)
    if (opt == js.undefined) {
      throw new IllegalStateException(s"Environment variable ${name} must be set. (Did you source .env)?")
    }
    opt.asInstanceOf[String]
  }

  def getOrElse(name: String, default: String): String = {
    val opt = js.`import`.meta.env.selectDynamic(name)
    if (opt == js.undefined) {
      return default
    }
    opt.asInstanceOf[String]
  }
}

object Globals {
  val VITE_SELENIUM: Boolean = Env.get("VITE_SELENIUM") == "true"

  val VITE_DATABASE_VERSION: String = Env.get("VITE_DATABASE_VERSION")

  lazy val VITE_PROTOCOL_VERSION: String = Env.getOrElse("VITE_PROTOCOL_VERSION", "test")

  lazy val VITE_SERVER_PROTOCOL: String = Env.get("VITE_SERVER_PROTOCOL")

  lazy val VITE_SERVER_HOST: String = Env.get("VITE_SERVER_HOST")

  lazy val VITE_SERVER_PATH: String = Env.get("VITE_SERVER_PATH")

  lazy val VITE_SERVER_PORT: String = Env.get("VITE_SERVER_PORT")

  lazy val VITE_DISCOVERY_SERVER_PROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_PROTOCOL")

  lazy val VITE_DISCOVERY_SERVER_HOST: String = Env.get("VITE_DISCOVERY_SERVER_HOST")

  lazy val VITE_DISCOVERY_SERVER_PATH: String = Env.get("VITE_DISCOVERY_SERVER_PATH")

  lazy val VITE_DISCOVERY_SERVER_LISTEN_PORT: String = Env.get("VITE_DISCOVERY_SERVER_LISTEN_PORT")

  lazy val VITE_DISCOVERY_SERVER_PUBLIC_PORT: String = Env.get("VITE_DISCOVERY_SERVER_PUBLIC_PORT")

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL")

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_HOST: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_HOST")

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PATH: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PATH")

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL")

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT")

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PUBLIC_PORT: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PUBLIC_PORT")

  lazy val VITE_TURN_SERVER_HOST: String = Env.get("VITE_TURN_SERVER_HOST")

  lazy val VITE_TURN_SERVER_PORT: String = Env.get("VITE_TURN_SERVER_PORT")

  lazy val VITE_ALWAYS_ONLINE_PEER_PROTOCOL: String = Env.get("VITE_ALWAYS_ONLINE_PEER_PROTOCOL")

  lazy val VITE_ALWAYS_ONLINE_PEER_HOST: String = Env.get("VITE_ALWAYS_ONLINE_PEER_HOST")

  lazy val VITE_ALWAYS_ONLINE_PEER_PATH: String = Env.get("VITE_ALWAYS_ONLINE_PEER_PATH")

  lazy val VITE_ALWAYS_ONLINE_PEER_SUBPROTOCOL: String = Env.get("VITE_ALWAYS_ONLINE_PEER_SUBPROTOCOL")

  lazy val VITE_ALWAYS_ONLINE_PEER_PUBLIC_PORT: String = Env.get("VITE_ALWAYS_ONLINE_PEER_PUBLIC_PORT")

  lazy val VITE_DEKANAT_MAIL: String = Env.get("VITE_DEKANAT_MAIL")

  lazy val APP_VERSION: String = js.Dynamic.global.APP_VERSION.asInstanceOf[String]
}
