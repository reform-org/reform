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
import webapp.Services
import rescala.default.*

object DiscoveryService {
  private var pendingConnections: Map[String, PendingConnection] = Map()
  private val ws: WebSocket = new WebSocket("wss://wss.discovery.lukasschreiber.com/")

  class LoginInfo(var username: String, var password: String)
  object LoginInfo {
    val codec: JsonValueCodec[LoginInfo] = JsonCodecMaker.make
  }

  class LoginRepsonse(var token: String, var username: String)
  object LoginRepsonse {
    val codec: JsonValueCodec[LoginRepsonse] = JsonCodecMaker.make
  }

  class TokenPayload(var exp: Int, var iat: Int, var username: String, var uuid: String)

  class AvailableConnection(
      var name: String,
      var uuid: String,
      var online: Boolean,
      var trusted: Boolean,
      var mutualTrust: Boolean,
  )

  private val setAvailableConnections = Evt[Seq[AvailableConnection]]()
  private val setAvailableConnectionsB = setAvailableConnections.act(identity)

  val availableConnections = Fold(Seq.empty: Seq[AvailableConnection])(setAvailableConnectionsB)

  def decodeToken(token: String): TokenPayload = {
    val decodedToken = JSON.parse(window.atob(token.split('.')(1))).asInstanceOf[js.Dynamic]
    TokenPayload(
      decodedToken.exp.asInstanceOf[Int],
      decodedToken.iat.asInstanceOf[Int],
      decodedToken.username.asInstanceOf[String],
      decodedToken.uuid.asInstanceOf[String],
    )
  }

  def getToken(): String = {
    window.localStorage.getItem("discovery-token")
  }

  private def tokenIsValid(token: String): Boolean = {
    return token != null && Date.now() > decodeToken(token).exp
  }

  def login(loginInfo: LoginInfo): Unit = {
    val savedToken = getToken()

    if (!tokenIsValid(savedToken)) {
      val requestHeaders = new Headers();
      requestHeaders.set("content-type", "application/json");
      fetch(
        "https://discovery.lukasschreiber.com/api/login",
        new RequestInit {
          method = HttpMethod.POST
          body = writeToString(loginInfo)(LoginInfo.codec)
          headers = requestHeaders
        },
      ).`then`(s =>
        s.json()
          .toFuture
          .onComplete(json => {
            val token = (json.get.asInstanceOf[js.Dynamic]).token.asInstanceOf[String]
            window.localStorage.setItem("discovery-token", token)
            console.log("Fetched a new token.")
          }),
      )
    }
  }

  private def emit(ws: WebSocket, name: String, payload: js.Dynamic) = {
    val event = js.Dynamic.literal("type" -> name, "payload" -> payload);
    ws.send(JSON.stringify(event))
  }

  private def handle(ws: WebSocket, name: String, payload: js.Dynamic)(using services: Services) = {
    console.log(name, payload)
    name match {
      case "request_host_token" => {
        var iceServers = js.Array[RTCIceServer]()
        iceServers += RTCIceServer(
          "turn:lukasschreiber.com:41720",
          payload.host.turn.username.asInstanceOf[String],
          payload.host.turn.credential.asInstanceOf[String],
        )
        iceServers += RTCIceServer("stun:lukasschreiber.com:41720")
        val config = RTCConfiguration(iceServers)
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.offer(config),
          payload.host.user.name.asInstanceOf[String],
        ))

        services.webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          pendingConnections(payload.id.asInstanceOf[String]).session.map(i => i.alias),
          "discovery",
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
          "turn:lukasschreiber.com:41720",
          payload.client.turn.username.asInstanceOf[String],
          payload.client.turn.credential.asInstanceOf[String],
        )
        iceServers += RTCIceServer("stun:lukasschreiber.com:41720")
        val config = RTCConfiguration(iceServers)
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.answer(config),
          payload.host.user.name.asInstanceOf[String],
        ))

        pendingConnections(payload.id.asInstanceOf[String]).connector
          .set(PendingConnection.tokenAsSession(payload.host.token.asInstanceOf[String]).session)

        services.webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          pendingConnections(payload.id.asInstanceOf[String]).session.map(i => i.alias),
          "discovery",
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
    }
  }

  def connect(using services: Services): Unit = {
    if (!tokenIsValid(getToken())) return;
    ws.onopen = (event) => {
      console.log("opened websocket")
      emit(ws, "authenticate", js.Dynamic.literal("token" -> getToken()))
    }

    ws.onmessage = (event) => {
      val json = JSON.parse(event.data.asInstanceOf[String])
      handle(ws, json.`type`.asInstanceOf[String], json.payload)(using services)
    }

    ws.onclose = (event) => {
      console.log("closed websocket")
    }
  }
}
