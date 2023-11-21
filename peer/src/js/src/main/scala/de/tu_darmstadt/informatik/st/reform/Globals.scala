package de.tu_darmstadt.informatik.st.reform

import scala.concurrent.ExecutionContext

// macrotask executor breaks indexeddb
given ExecutionContext = scala.concurrent.ExecutionContext.global

import de.tu_darmstadt.informatik.st.reform.npm.IIndexedDB
import de.tu_darmstadt.informatik.st.reform.services.{DiscoveryService, MailService, RoutingService, Toaster}
import de.tu_darmstadt.informatik.st.reform.webrtc.WebRTCService
import loci.registry.Registry

import scala.scalajs.js

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
  private val env = js.`import`.meta.env

  def get(name: String): String = {
    val opt: Option[String] = maybeGet(name)

    if (opt.isEmpty) {
      throw new IllegalStateException(s"Environment variable ${name} must be set. (Did you source .env)?")
    }

    opt.get
  }

  def getOrElse(name: String, default: String): String = {
    maybeGet(name).getOrElse(default)
  }

  private def maybeGet(name: String): Option[String] = {
    if (env == js.undefined) {
      return sys.env.get(name)
    }

    val x = env.selectDynamic(name)

    if (x == js.undefined) {
      return None
    }

    Some(x.asInstanceOf[String])
  }
}

object Globals {
  lazy val VITE_SELENIUM: Boolean = Env.getOrElse("VITE_SELENIUM", "false") == "true"

  lazy val VITE_DATABASE_VERSION: String = Env.get("VITE_DATABASE_VERSION")

  lazy val VITE_PROTOCOL_VERSION: String = Env.getOrElse("VITE_PROTOCOL_VERSION", "test")

  lazy val VITE_SERVER_PROTOCOL: String = Env.get("VITE_SERVER_PROTOCOL")
  lazy val VITE_SERVER_HOST: String = Env.get("VITE_SERVER_HOST")
  lazy val VITE_SERVER_PATH: String = Env.get("VITE_SERVER_PATH")
  lazy val VITE_SERVER_PORT: String = Env.get("VITE_SERVER_PORT")

  lazy val VITE_DISCOVERY_SERVER_PROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_PROTOCOL")
  lazy val VITE_DISCOVERY_SERVER_HOST: String = Env.get("VITE_DISCOVERY_SERVER_HOST")
  lazy val VITE_DISCOVERY_SERVER_PATH: String = Env.get("VITE_DISCOVERY_SERVER_PATH")
  // TODO: whats the difference between the two?
  lazy val VITE_DISCOVERY_SERVER_LISTEN_PORT: String = Env.get("VITE_DISCOVERY_SERVER_LISTEN_PORT")
  lazy val VITE_DISCOVERY_SERVER_PUBLIC_PORT: String = Env.get("VITE_DISCOVERY_SERVER_PUBLIC_PORT")

  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL")
  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_HOST: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_HOST")
  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_PATH: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PATH")
  lazy val VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL")
  // TODO: whats the difference between the two?
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

  lazy val DISCOVERY_SERVER_URL = s"${VITE_DISCOVERY_SERVER_PROTOCOL}://${VITE_DISCOVERY_SERVER_HOST}:${VITE_DISCOVERY_SERVER_PUBLIC_PORT}${VITE_DISCOVERY_SERVER_PATH}"
}
