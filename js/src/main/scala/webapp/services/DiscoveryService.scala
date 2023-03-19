package webapp.services

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.communicator.webrtc.WebRTC
import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default.*
import webapp.Globals
import webapp.webrtc.PendingConnection
import webapp.webrtc.WebRTCService

import webapp.given_ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.JSON
import webapp.utils.Futures.*
import loci.transmitter.RemoteRef
import webapp.{*, given}
import scala.annotation.nowarn
import scala.util.Try
import loci.communicator.ws.webnative.WS
import loci.registry.Registry
import webapp.JSImplicits
import webapp.utils.Cookies

class AvailableConnection(
    val name: String,
    val uuid: String,
    val displayId: String,
    val trusted: Boolean,
    val mutualTrust: Boolean,
)

class LoginException(val message: String, val fields: Seq[String]) extends Throwable(message)

class LoginInfo(val username: String, val password: String)
object LoginInfo {
  val codec: JsonValueCodec[LoginInfo] = JsonCodecMaker.make
}

class DiscoveryService(using toaster: Toaster) {
  private var pendingConnections: Map[String, PendingConnection] = Map()
  private var ws: Option[WebSocket] = None

  class LoginRepsonse(val token: String, val username: String)
  object LoginRepsonse {
    val codec: JsonValueCodec[LoginRepsonse] = JsonCodecMaker.make
  }

  class TokenPayload(val exp: Int, val iat: Int, val username: String, val uuid: String)

  val availableConnections: Var[Seq[AvailableConnection]] = Var(Seq.empty)

  val token: Var[Option[String]] = Var(Cookies.getCookie("discovery-token"))

  val online: Var[Boolean] = Var(false)

  def setAutoconnect(using jsImplicits: JSImplicits)(
      value: Boolean,
  ): Unit = {
    window.localStorage.setItem("autoconnect", value.toString)
    autoconnect.set(value)
    if (value == true) {
      connect()
        .toastOnError()
    } else {
      close()
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
        Cookies.setCookie("discovery-token", value)
      }
      case None => {
        Cookies.clearCookie("discovery-token")
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

  def login(using jsImplicits: JSImplicits)(
      loginInfo: LoginInfo,
  ): Future[String] = {
    val promise = Promise[String]()

    if (!tokenIsValid(token.now)) {
      val requestHeaders = new Headers();
      requestHeaders.set("content-type", "application/json");
      fetch(
        s"${Globals.VITE_DISCOVERY_SERVER_PROTOCOL}://${Globals.VITE_DISCOVERY_SERVER_HOST}:${Globals.VITE_DISCOVERY_SERVER_PUBLIC_PORT}${Globals.VITE_DISCOVERY_SERVER_PATH}/login",
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
              connect()
                .toastOnError()
              promise.success(newToken)
            }
          })
      }).toFuture
        .toastOnError()
    }

    promise.future
  }

  def disconnect(using jsImplicits: JSImplicits)(ref: RemoteRef): Unit = {
    reportClosedConnection(jsImplicits.webrtc.getInformation(ref).connectionId)
    ref.disconnect()
  }

  def addToWhitelist(uuid: String): Unit = {
    ws.foreach(emit(_, "whitelist_add", js.Dynamic.literal("uuid" -> uuid)))
  }

  def connectTo(uuid: String): Unit = {
    ws.foreach(emit(_, "connect_to", js.Dynamic.literal("uuid" -> uuid)))
  }

  def reportClosedConnection(id: String): Unit = {
    ws.foreach(emit(_, "connection_closed", js.Dynamic.literal("connection" -> id)))
  }

  def deleteFromWhitelist(uuid: String): Unit = {
    ws.foreach(emit(_, "whitelist_del", js.Dynamic.literal("uuid" -> uuid)))
  }

  def refetchAvailableClients(): Unit = {
    ws.foreach(emit(_, "request_available_clients", null))
  }

  private def emit(ws: WebSocket, name: String, payload: js.Any | Null) = {
    val event = js.Dynamic.literal("type" -> name, "payload" -> payload.asInstanceOf[js.Any])
    ws.send(JSON.stringify(event))
  }

  private def getRTCIceServers(payload: js.Dynamic): RTCConfiguration = {
    // https://developer.mozilla.org/en-US/docs/Web/API/RTCIceServer/urls
    new RTCConfiguration {
      iceServers = js.Array(
        new RTCIceServer {
          urls = s"stun:${Globals.VITE_TURN_SERVER_HOST}:${Globals.VITE_TURN_SERVER_PORT}";
        },
        new RTCIceServer {
          urls = s"turn:${Globals.VITE_TURN_SERVER_HOST}:${Globals.VITE_TURN_SERVER_PORT}";
          username = payload.client.turnKey.username.asInstanceOf[String];
          credential = payload.client.turnKey.credential.asInstanceOf[String];
        },
      );
    }
  }

  private def handle(using jsImplicits: JSImplicits)(ws: WebSocket, name: String, payload: js.Dynamic) = {
    if (name != "ping") console.log(name, payload)

    name match {
      case "request_host_token" => {
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.offer(getRTCIceServers(payload)),
          payload.client.user.name.asInstanceOf[String],
        ))

        pendingConnections(payload.id.asInstanceOf[String]).session
          .map(PendingConnection.sessionAsToken)
          .foreach(token => {
            emit(ws, "host_token", js.Dynamic.literal("token" -> token, "connection" -> payload.id))
          })

        jsImplicits.webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          payload.client.user.name.asInstanceOf[String],
          "discovery",
          pendingConnections(payload.id.asInstanceOf[String]).connection,
          payload.client.user.id.asInstanceOf[String],
          payload.client.user.displayId.asInstanceOf[String],
          payload.id.asInstanceOf[String],
        )
      }
      case "request_client_token" => {
        pendingConnections += (payload.id.asInstanceOf[String] -> PendingConnection.webrtcIntermediate(
          WebRTC.answer(getRTCIceServers(payload)),
          payload.host.user.name.asInstanceOf[String],
        ))

        pendingConnections(payload.id.asInstanceOf[String]).connector
          .set(PendingConnection.tokenAsSession(payload.host.token.asInstanceOf[String]).session)

        pendingConnections(payload.id.asInstanceOf[String]).session
          .map(PendingConnection.sessionAsToken)
          .foreach(token => {
            emit(ws, "client_token", js.Dynamic.literal("token" -> token, "connection" -> payload.id))
          })

        jsImplicits.webrtc.registerConnection(
          pendingConnections(payload.id.asInstanceOf[String]).connector,
          payload.host.user.name.asInstanceOf[String],
          "discovery",
          pendingConnections(payload.id.asInstanceOf[String]).connection,
          payload.host.user.id.asInstanceOf[String],
          payload.host.user.displayId.asInstanceOf[String],
          payload.id.asInstanceOf[String],
        )
      }
      case "available_clients" => {
        val clients = payload.clients
          .asInstanceOf[js.Array[js.Dynamic]]
          .map(client =>
            new AvailableConnection(
              client.name.asInstanceOf[String],
              client.id.asInstanceOf[String],
              client.displayId.asInstanceOf[String],
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
        emit(ws, "pong", null)
      }
      case "connection_closed" => {
        jsImplicits.webrtc.closeConnectionById(payload.id.asInstanceOf[String])
      }
    }
  }

  def close() = {
    ws.map(ws => ws.close())
    ws = None
  }

  def connect(using
      jsImplicits: JSImplicits,
  )(resetWebsocket: Boolean = false, force: Boolean = false): Future[Boolean] = {
    val promise = Promise[Boolean]()

    if (resetWebsocket) ws = None

    if (Option(window.localStorage.getItem("autoconnect")).getOrElse("true").toBoolean || force) {
      if (tokenIsValid(token.now)) {
        ws match {
          case Some(socket) => {}
          case None =>
            ws = Some(
              new WebSocket(
                s"${Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL}://${Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_HOST}:${Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_PUBLIC_PORT}${Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_PATH}",
                Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_SUBPROTOCOL,
              ),
            )
        }

        ws.get.onopen = (_) => {
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
        }

        ws.get.onerror = (_) => {
          promise.failure(new Exception("Connection failed"))
        }

        Try({
          val ws = WS(
            s"${Globals.VITE_ALWAYS_ONLINE_PEER_PROTOCOL}://${Globals.VITE_ALWAYS_ONLINE_PEER_HOST}:${Globals.VITE_ALWAYS_ONLINE_PEER_PUBLIC_PORT}${Globals.VITE_ALWAYS_ONLINE_PEER_PATH}?${token.now
                .getOrElse("")}",
          )
          jsImplicits.registry
            .connect(
              ws,
            )
            .toastOnError(ToastMode.Short, ToastType.Warning)
        }).toastOnError(ToastMode.Short, ToastType.Warning)

        promise.future
      } else {
        promise.failure(new Exception("Your token is wrong")).future
      }
    } else {
      Future.successful(true)
    }
  }
}
