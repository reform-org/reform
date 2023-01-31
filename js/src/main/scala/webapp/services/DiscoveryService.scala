package webapp.services

import org.scalajs.dom.*
import org.scalajs.dom
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import scala.util.Try
import scala.scalajs.js.JSON
import scala.scalajs.js.Date
import webapp.webrtc.PendingConnection
import loci.communicator.webrtc.WebRTC
import rescala.default.*
import webapp.Globals
import webapp.Settings
import scala.concurrent.{Future, Promise}
import webapp.webrtc.WebRTCService

class AvailableConnection(
    var name: String,
    var uuid: String,
    var online: Boolean,
    var trusted: Boolean,
    var mutualTrust: Boolean,
)

class DiscoveryService {
  private var pendingConnections: Map[String, PendingConnection] = Map()
  private var ws: Option[WebSocket] = None

  class LoginInfo(var username: String, var password: String)
  object LoginInfo {
    val codec: JsonValueCodec[LoginInfo] = JsonCodecMaker.make
  }

  class LoginRepsonse(var token: String, var username: String)
  object LoginRepsonse {
    val codec: JsonValueCodec[LoginRepsonse] = JsonCodecMaker.make
  }

  class LoginException(var message: String, var fields: Seq[String]) extends Throwable(message)

  class TokenPayload(var exp: Int, var iat: Int, var username: String, var uuid: String)

  private val setAvailableConnections = Evt[Seq[AvailableConnection]]()
  private val setAvailableConnectionsB = setAvailableConnections.act(identity)

  val availableConnections = Fold(Seq.empty: Seq[AvailableConnection])(setAvailableConnectionsB)

  private val setToken = Evt[String]()
  private val setTokenB = setToken.act(identity)

  private val token = Fold(null: String)(setTokenB)

  private val setOnlineStatus = Evt[Boolean]()
  private val setOnlineStatusB = setOnlineStatus.act(identity)

  val online = Fold(false: Boolean)(setOnlineStatusB)

  def setAutoconnect(value: Boolean)(using discovery: DiscoveryService, webrtc: WebRTCService): Unit = {
    Settings.set[Boolean]("autoconnect", value)
    if (value == true) {
      console.log("should connect")
      discovery.connect()
    }
  }

  def decodeToken(token: String): TokenPayload = {
    val decodedToken = JSON.parse(window.atob(token.split('.')(1))).asInstanceOf[js.Dynamic]
    TokenPayload(
      decodedToken.exp.asInstanceOf[Int],
      decodedToken.iat.asInstanceOf[Int],
      decodedToken.username.asInstanceOf[String],
      decodedToken.uuid.asInstanceOf[String],
    )
  }

  def getTokenSignal(): Signal[String] = {
    getToken()
    token
  }

  def getToken(): String = {
    val storedToken = window.localStorage.getItem("discovery-token")
    setToken.fire(storedToken)
    storedToken
  }

  def tokenIsValid(token: String): Boolean = {
    return token != null && !token.isBlank() && Date.now() > decodeToken(token).exp
  }

  def logout(): Unit = {
    ws match {
      case None         => {}
      case Some(socket) => ws.get.close()
    }
    window.localStorage.removeItem("discovery-token")
    setToken.fire(null)
  }

  def login(loginInfo: LoginInfo)(using webrtc: WebRTCService): Future[String] = {
    val savedToken = getToken()
    val promise = Promise[String]()

    if (!tokenIsValid(savedToken)) {
      val requestHeaders = new Headers();
      requestHeaders.set("content-type", "application/json");
      fetch(
        s"${Globals.discoveryServerURL}/api/login",
        new RequestInit {
          method = HttpMethod.POST
          body = writeToString(loginInfo)(LoginInfo.codec)
          headers = requestHeaders
        },
      ).`then`(s => {
        s.json()
          .toFuture
          .onComplete(json => {
            if (s.status > 400 && s.status < 500) {
              val error = (json.get.asInstanceOf[js.Dynamic]).error;
              promise.failure(
                new LoginException(
                  error.message.asInstanceOf[String],
                  error.fields.asInstanceOf[js.Array[String]].toSeq,
                ),
              )
            } else {
              val token = (json.get.asInstanceOf[js.Dynamic]).token.asInstanceOf[String]
              window.localStorage.setItem("discovery-token", token)
              setToken.fire(token)
              console.log("Fetched a new token.")
              this.connect()
              promise.success(token)
            }
          })
      })
    }

    promise.future
  }

  def addToWhitelist(uuid: String): Unit = {
    ws match {
      case None         => {}
      case Some(socket) => emit(socket, "whitelist_add", js.Dynamic.literal("uuid" -> uuid))
    }
  }

  def deleteFromWhitelist(uuid: String): Unit = {
    ws match {
      case None         => {}
      case Some(socket) => emit(socket, "whitelist_del", js.Dynamic.literal("uuid" -> uuid))
    }
  }

  def refetchAvailableClients(): Unit = {
    ws match {
      case None         => {}
      case Some(socket) => emit(socket, "request_available_clients", null)
    }
  }

  private def emit(ws: WebSocket, name: String, payload: js.Dynamic) = {
    val event = js.Dynamic.literal("type" -> name, "payload" -> payload);
    ws.send(JSON.stringify(event))
  }

  private def handle(ws: WebSocket, name: String, payload: js.Dynamic)(using webrtc: WebRTCService) = {
    console.log(name, payload)
    name match {
      case "request_host_token" => {
        var iceServers = js.Array[RTCIceServer]()
        iceServers += RTCIceServer(
          Globals.turnServerURL,
          payload.host.turn.username.asInstanceOf[String],
          payload.host.turn.credential.asInstanceOf[String],
        )
        // iceServers += RTCIceServer("stun:lukasschreiber.com:41720")
        val config = RTCConfiguration(iceServers)
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.offer(config),
          payload.client.user.name.asInstanceOf[String],
        ))

        webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          pendingConnections(payload.id.asInstanceOf[String]).session.map(i => i.alias),
          "discovery",
          pendingConnections(payload.id.asInstanceOf[String]).connection,
          payload.client.user.uuid.asInstanceOf[String],
          payload.id.asInstanceOf[String],
        )

        pendingConnections(payload.id.asInstanceOf[String]).session
          .map(PendingConnection.sessionAsToken)
          .map(token => {
            emit(ws, "host_token", js.Dynamic.literal("token" -> token, "connection" -> payload.id))
          })
      }
      case "request_client_token" => {
        var iceServers = js.Array[RTCIceServer]()
        iceServers += RTCIceServer(
          Globals.turnServerURL,
          payload.client.turn.username.asInstanceOf[String],
          payload.client.turn.credential.asInstanceOf[String],
        )
        // iceServers += RTCIceServer("stun:lukasschreiber.com:41720")
        val config = RTCConfiguration(iceServers)
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.answer(config),
          payload.host.user.name.asInstanceOf[String],
        ))

        pendingConnections(payload.id.asInstanceOf[String]).connector
          .set(PendingConnection.tokenAsSession(payload.host.token.asInstanceOf[String]).session)

        webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          pendingConnections(payload.id.asInstanceOf[String]).session.map(i => i.alias),
          "discovery",
          pendingConnections(payload.id.asInstanceOf[String]).connection,
          payload.host.user.uuid.asInstanceOf[String],
          payload.id.asInstanceOf[String],
        )

        pendingConnections(payload.id.asInstanceOf[String]).session
          .map(PendingConnection.sessionAsToken)
          .map(token => {
            emit(ws, "client_token", js.Dynamic.literal("token" -> token, "connection" -> payload.id))
          })
      }
      case "available_clients" => {
        val clients = payload.clients
          .asInstanceOf[js.Array[js.Dynamic]]
          .map(client =>
            new AvailableConnection(
              client.name.asInstanceOf[String],
              client.uuid.asInstanceOf[String],
              client.online.asInstanceOf[Int] != 0,
              client.trusted.asInstanceOf[Int] != 0,
              client.mutualTrust.asInstanceOf[Int] != 0,
            ),
          )
        var clientsSeq: Seq[AvailableConnection] = clients.toSeq
        setAvailableConnections.fire(clientsSeq)
      }
      case "request_client_finish_connection" => {
        pendingConnections -= payload.id.asInstanceOf[String]
        emit(ws, "finish_connection", js.Dynamic.literal("connection" -> payload.id))
      }
      case "request_host_finish_connection" => {
        pendingConnections(payload.id.asInstanceOf[String]).connector
          .set(PendingConnection.tokenAsSession(payload.client.token.asInstanceOf[String]).session)
        pendingConnections -= payload.id.asInstanceOf[String]
        emit(ws, "finish_connection", js.Dynamic.literal("connection" -> payload.id))
      }
      case "ping" => {
        emit(ws, "pong", null)
      }
      case "connection_closed" => {
        webrtc.closeConnectionById(payload.id.asInstanceOf[String])
      }
    }
  }

  def connect(resetWebsocket: Boolean = false)(using webrtc: WebRTCService): Future[Boolean] = {
    val promise = Promise[Boolean]()

    if (resetWebsocket) ws = None

    if (tokenIsValid(getToken()) && Settings.get[Boolean]("autoconnect").getOrElse(false)) {
      ws match {
        case Some(socket) => {}
        case None         => ws = Some(new WebSocket(Globals.discoveryServerWebsocketURL))
      }

      ws.get.onopen = (event) => {
        console.log("opened websocket")
        setOnlineStatus.fire(true)
        emit(ws.get, "authenticate", js.Dynamic.literal("token" -> getToken()))
        promise.success(true)
      }

      ws.get.onmessage = (event) => {
        val json = JSON.parse(event.data.asInstanceOf[String])
        handle(ws.get, json.`type`.asInstanceOf[String], json.payload)
      }

      ws.get.onclose = (event) => {
        setOnlineStatus.fire(false)
        console.log("closed websocket")
      }

      ws.get.onerror = (event) => {
        promise.failure(new Exception("Connection failed"))
      }
    } else {
      promise.failure(new Exception("Something went wrong"))
    }

    promise.future
  }
}
