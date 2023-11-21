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
  val VITE_SELENIUM: Boolean = Env.getOrElse("VITE_SELENIUM", "false") == "true"

  val VITE_DATABASE_VERSION: String = Env.get("VITE_DATABASE_VERSION")

  val VITE_PROTOCOL_VERSION: String = Env.getOrElse("VITE_PROTOCOL_VERSION", "test")

  val VITE_SERVER_PROTOCOL: String = Env.get("VITE_SERVER_PROTOCOL")
  val VITE_SERVER_HOST: String = Env.get("VITE_SERVER_HOST")
  val VITE_SERVER_PATH: String = Env.get("VITE_SERVER_PATH")
  val VITE_SERVER_PORT: String = Env.get("VITE_SERVER_PORT")

  val VITE_DISCOVERY_SERVER_PROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_PROTOCOL")
  val VITE_DISCOVERY_SERVER_HOST: String = Env.get("VITE_DISCOVERY_SERVER_HOST")
  val VITE_DISCOVERY_SERVER_PATH: String = Env.get("VITE_DISCOVERY_SERVER_PATH")
  // TODO: whats the difference between the two?
  val VITE_DISCOVERY_SERVER_LISTEN_PORT: String = Env.get("VITE_DISCOVERY_SERVER_LISTEN_PORT")
  val VITE_DISCOVERY_SERVER_PUBLIC_PORT: String = Env.get("VITE_DISCOVERY_SERVER_PUBLIC_PORT")

  val VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL")
  val VITE_DISCOVERY_SERVER_WEBSOCKET_HOST: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_HOST")
  val VITE_DISCOVERY_SERVER_WEBSOCKET_PATH: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PATH")
  val VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL")
  // TODO: whats the difference between the two?
  val VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT")
  val VITE_DISCOVERY_SERVER_WEBSOCKET_PUBLIC_PORT: String = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_PUBLIC_PORT")

  val VITE_TURN_SERVER_HOST: String = Env.get("VITE_TURN_SERVER_HOST")
  val VITE_TURN_SERVER_PORT: String = Env.get("VITE_TURN_SERVER_PORT")
  val VITE_ALWAYS_ONLINE_PEER_PROTOCOL: String = Env.get("VITE_ALWAYS_ONLINE_PEER_PROTOCOL")
  val VITE_ALWAYS_ONLINE_PEER_HOST: String = Env.get("VITE_ALWAYS_ONLINE_PEER_HOST")

  val VITE_ALWAYS_ONLINE_PEER_PATH: String = Env.get("VITE_ALWAYS_ONLINE_PEER_PATH")
  val VITE_ALWAYS_ONLINE_PEER_SUBPROTOCOL: String = Env.get("VITE_ALWAYS_ONLINE_PEER_SUBPROTOCOL")
  val VITE_ALWAYS_ONLINE_PEER_PUBLIC_PORT: String = Env.get("VITE_ALWAYS_ONLINE_PEER_PUBLIC_PORT")

  val VITE_DEKANAT_MAIL: String = Env.get("VITE_DEKANAT_MAIL")

  lazy val APP_VERSION: String = js.Dynamic.global.APP_VERSION.asInstanceOf[String]

  lazy val DISCOVERY_SERVER_URL = s"${VITE_DISCOVERY_SERVER_PROTOCOL}://${VITE_DISCOVERY_SERVER_HOST}:${VITE_DISCOVERY_SERVER_PUBLIC_PORT}${VITE_DISCOVERY_SERVER_PATH}"
}
