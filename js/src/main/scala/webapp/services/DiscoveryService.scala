package webapp.services

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.communicator.webrtc.WebRTC
import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default.*
import webapp.Globals
import webapp.Settings
import webapp.webrtc.PendingConnection
import webapp.webrtc.WebRTCService

import webapp.given_ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.JSON
import webapp.utils.Futures.*

class AvailableConnection(
    val name: String,
    val uuid: String,
    val online: Boolean,
    val trusted: Boolean,
    val mutualTrust: Boolean,
)

class DiscoveryService {
  private var pendingConnections: Map[String, PendingConnection] = Map()
  private var ws: Option[WebSocket] = None

  class LoginInfo(val username: String, val password: String)
  object LoginInfo {
    val codec: JsonValueCodec[LoginInfo] = JsonCodecMaker.make
  }

  class LoginRepsonse(val token: String, val username: String)
  object LoginRepsonse {
    val codec: JsonValueCodec[LoginRepsonse] = JsonCodecMaker.make
  }

  class LoginException(val message: String, val fields: Seq[String]) extends Throwable(message)

  class TokenPayload(val exp: Int, val iat: Int, val username: String, val uuid: String)

  val availableConnections: Var[Seq[AvailableConnection]] = Var(Seq.empty)

  val token: Var[Option[String]] = Var(Option(window.localStorage.getItem("discovery-token")))

  val online: Var[Boolean] = Var(false)

  def setAutoconnect(
      value: Boolean,
  )(using discovery: DiscoveryService, webrtc: WebRTCService, toaster: Toaster): Unit = {
    Settings.set[Boolean]("autoconnect", value)
    if (value == true) {
      console.log("should connect")
      discovery
        .connect()
        .toastOnError()
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

  def updateToken(value: Option[String]) = {
    value match {
      case Some(value) => {
        window.localStorage.setItem("discovery-token", value)
      }
      case None => {
        window.localStorage.removeItem("discovery-token")
      }
    }
    token.set(value)
  }

  def tokenIsValid(token: Option[String]): Boolean = {
    return token.nonEmpty && !token.get.isBlank() && Date.now() > decodeToken(token.get).exp
  }

  def logout(): Unit = {
    ws match {
      case None         => {}
      case Some(socket) => ws.get.close()
    }
    updateToken(None)
  }

  def login(loginInfo: LoginInfo)(using webrtc: WebRTCService, toaster: Toaster): Future[String] = {
    val promise = Promise[String]()

    if (!tokenIsValid(token.now)) {
      val requestHeaders = new Headers();
      requestHeaders.set("content-type", "application/json");
      fetch(
        s"${Globals.VITE_DISCOVERY_SERVER_PROTOCOL}://${Globals.VITE_DISCOVERY_SERVER_HOST}:${Globals.VITE_DISCOVERY_SERVER_PORT}/api/login",
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
              val newToken = (json.get.asInstanceOf[js.Dynamic]).token.asInstanceOf[String]
              updateToken(Some(newToken))
              console.log("Fetched a new token.")
              this
                .connect()
                .toastOnError()
              promise.success(newToken)
            }
          })
      }).toFuture
        .toastOnError()
    }

    promise.future
  }

  def addToWhitelist(uuid: String): Unit = {
    ws.foreach(emit(_, "whitelist_add", js.Dynamic.literal("uuid" -> uuid)))
  }

  def deleteFromWhitelist(uuid: String): Unit = {
    ws.foreach(emit(_, "whitelist_del", js.Dynamic.literal("uuid" -> uuid)))
  }

  def refetchAvailableClients(): Unit = {
    ws.foreach(emit(_, "request_available_clients", None.orNull))
  }

  private def emit(ws: WebSocket, name: String, payload: js.Dynamic) = {
    val event = js.Dynamic.literal("type" -> name, "payload" -> payload);
    ws.send(JSON.stringify(event))
  }

  private def handle(ws: WebSocket, name: String, payload: js.Dynamic)(using webrtc: WebRTCService) = {
    console.log(name, payload)
    name match {
      case "request_host_token" => {
        val config = new RTCConfiguration {
          iceServers = js.Array(
            new RTCIceServer {
              urls = Globals.VITE_TURN_SERVER;
              username = payload.host.turn.username.asInstanceOf[String];
              credential = payload.host.turn.credential.asInstanceOf[String];
            },
          );
        }
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.offer(config),
          payload.client.user.name.asInstanceOf[String],
        ))

        pendingConnections(payload.id.asInstanceOf[String]).session
          .map(PendingConnection.sessionAsToken)
          .foreach(token => {
            emit(ws, "host_token", js.Dynamic.literal("token" -> token, "connection" -> payload.id))
          })

        webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          payload.client.user.name.asInstanceOf[String],
          "discovery",
          pendingConnections(payload.id.asInstanceOf[String]).connection,
          payload.client.user.uuid.asInstanceOf[String],
          payload.id.asInstanceOf[String],
        )
      }
      case "request_client_token" => {
        val config = new RTCConfiguration {
          iceServers = js.Array(new RTCIceServer {
            urls = Globals.VITE_TURN_SERVER;
            username = payload.client.turn.username.asInstanceOf[String];
            credential = payload.client.turn.credential.asInstanceOf[String];
          });
        }
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.answer(config),
          payload.host.user.name.asInstanceOf[String],
        ))

        pendingConnections(payload.id.asInstanceOf[String]).connector
          .set(PendingConnection.tokenAsSession(payload.host.token.asInstanceOf[String]).session)

        pendingConnections(payload.id.asInstanceOf[String]).session
          .map(PendingConnection.sessionAsToken)
          .foreach(token => {
            emit(ws, "client_token", js.Dynamic.literal("token" -> token, "connection" -> payload.id))
          })

        webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          payload.host.user.name.asInstanceOf[String],
          "discovery",
          pendingConnections(payload.id.asInstanceOf[String]).connection,
          payload.host.user.uuid.asInstanceOf[String],
          payload.id.asInstanceOf[String],
        )
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
        availableConnections.set(clientsSeq)
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
        emit(ws, "pong", None.orNull)
      }
      case "connection_closed" => {
        webrtc.closeConnectionById(payload.id.asInstanceOf[String])
      }
    }
  }

  def connect(resetWebsocket: Boolean = false)(using webrtc: WebRTCService): Future[Boolean] = {
    val promise = Promise[Boolean]()

    if (resetWebsocket) ws = None

    if (Settings.get[Boolean]("autoconnect").getOrElse(false)) {
      if (tokenIsValid(token.now)) {
        ws match {
          case Some(socket) => {}
          case None         => ws = Some(new WebSocket(Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_URL))
        }

        ws.get.onopen = (_) => {
          console.log("opened websocket")
          online.set(true)
          emit(ws.get, "authenticate", js.Dynamic.literal("token" -> token.now.orNull.nn))
          promise.success(true)
        }

        ws.get.onmessage = (event) => {
          val json = JSON.parse(event.data.asInstanceOf[String])
          handle(ws.get, json.`type`.asInstanceOf[String], json.payload)
        }

        ws.get.onclose = (_) => {
          online.set(false)
          console.log("closed websocket")
        }

        ws.get.onerror = (_) => {
          promise.failure(new Exception("Connection failed"))
        }
        promise.future
      } else {
        promise.failure(new Exception("Your token is wrong")).future
      }
    } else {
      Future.successful(true)
    }
  }
}
