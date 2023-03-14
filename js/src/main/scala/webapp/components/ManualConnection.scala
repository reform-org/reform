package webapp.components

import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import org.scalajs.dom
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.given
import webapp.webrtc.PendingConnection
import webapp.webrtc.WebRTCService
import webapp.services.Toaster
import webapp.given_ExecutionContext
import webapp.utils.Futures.*
import loci.transmitter.RemoteRef
import webapp.components.common.Input
import webapp.components.common.LabeledInput
import webapp.components.common.Button
import webapp.components.common.ButtonStyle
import webapp.components.icons
import org.scalajs.dom.RTCConfiguration
import scala.scalajs.js
import org.scalajs.dom.RTCIceServer

private val webrtcConfig = new RTCConfiguration {
  iceServers = js.Array(
    new RTCIceServer {
      urls = s"stun:${Globals.VITE_TURN_SERVER_HOST}:${Globals.VITE_TURN_SERVER_PORT}";
    },
  );
}

private sealed trait State {
  def render(using state: Var[State]): VNode
}

private def showConnectionToken(connection: PendingConnection)(using jsImplicits: JSImplicits) = {
  connection.session.map(session => {
    div(
      cls := "flex gap-1 mt-2",
      button(
        data.token := PendingConnection.sessionAsToken(session),
        cls := "w-fit h-fit btn btn-square rounded-xl bg-purple-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
        icons.Clipboard(cls := "w-6 h-6 text-white group-hover:text-purple-600"),
        onClick.foreach(_ =>
          window.navigator.clipboard
            .writeText(PendingConnection.sessionAsToken(session))
            .toFuture
            .toastOnError(),
        ),
      ),
      a(
        cls := "w-fit h-fit btn btn-square rounded-xl bg-purple-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
        icons.Mail(cls := "w-6 h-6 text-white group-hover:text-purple-600"),
        href := s"mailto:?subject=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${PendingConnection
            .sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
      ),
      a(
        cls := "w-fit h-fit btn btn-square rounded-xl bg-green-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
        icons.Whatsapp(cls := "w-6 h-6 group-hover:text-green-600 text-white"),
        href := s"whatsapp://send?text=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${PendingConnection
            .sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
      ),
    )
  })
}

private case class Init()(using jsImplicits: JSImplicits) extends State {
  private def initializeHostSession(using state: Var[State]): Unit = {
    val pendingConnection =
      PendingConnection.webrtcIntermediate(WebRTC.offer(webrtcConfig), alias.now)
    state.set(HostPending(pendingConnection))
  }

  private val alias = Var("")
  override def render(using state: Var[State]): VNode = {
    div(
      cls := "form-control w-full text-sm",
      LabeledInput("What is your name?")(
        tpe := "text",
        placeholder := "Your name",
        onInput.value --> alias,
        value := "",
      ),
      Button(
        ButtonStyle.Primary,
        cls := "w-full mt-2",
        "Create Invitation",
        disabled <-- Signal { alias.value.isBlank() },
        onClick.foreach(_ => initializeHostSession),
      ),
    )
  }
}

private case class ClientAskingForHostSessionToken()(using jsImplicits: JSImplicits) extends State {
  private val sessionToken = Var("")
  private val alias = Var("")
  override def render(using state: Var[State]): VNode = div(
    cls := "p1",
    LabeledInput("What is your name?")(tpe := "text", placeholder := "Your name", onInput.value --> alias, value := ""),
    LabeledInput("Please enter the code your peer has provided:")(
      tpe := "text",
      placeholder := "Token",
      cls := "input input-bordered w-full text-sm p-2 h-fit",
      value := "",
      onInput.value --> sessionToken,
    ),
    Button(
      ButtonStyle.Primary,
      "Connect",
      cls := "w-full",
      disabled <-- Signal { alias.value.isBlank() || sessionToken.value.isBlank() },
      onClick.foreach(_ => connectToHost),
    ),
  )

  private def connectToHost(using state: Var[State]): Unit = {
    val connection = PendingConnection.webrtcIntermediate(WebRTC.answer(webrtcConfig), alias.now)
    connection.connector.set(PendingConnection.tokenAsSession(sessionToken.now).session)
    state.set(ClientWaitingForHostConfirmation(connection, PendingConnection.tokenAsSession(sessionToken.now).alias))
  }
}

private case class ClientWaitingForHostConfirmation(connection: PendingConnection, alias: String)(using
    state: Var[State],
    jsImplicits: JSImplicits,
) extends State {
  jsImplicits.webrtc
    .registerConnection(connection.connector, alias, "manual", connection.connection)
    .foreach(_ => onConnected())

  override def render(using state: Var[State]): VNode = div(
    cls := "p-1",
    span(
      cls := "label label-text text-slate-500 dark:text-gray-300",
      "Please share the code with the peer that invited you to finish the connection.",
    ),
    showConnectionToken(connection),
  )

  private def onConnected()(using state: Var[State]): Unit = {
    state.set(ClientAskingForHostSessionToken())
  }
}

private case class HostPending(connection: PendingConnection)(using
    state: Var[State],
    jsImplicits: JSImplicits,
) extends State {
  private val sessionTokenFromClient = Var("")

  jsImplicits.webrtc
    .registerConnection(
      connection.connector,
      "Anonymous",
      "manual",
      connection.connection,
      "",
      "",
      "",
      onConnected,
    )
    .foreach(ref => onConnected(ref))

  override def render(using state: Var[State]): VNode = div(
    cls := "p-1",
    span(
      cls := "label label-text text-slate-500 dark:text-gray-300",
      "Please share the Invitation with one peer. The peer will respond with an code which finishes the connection.",
    ),
    showConnectionToken(connection),
    LabeledInput("Please enter the code your peer has provided:")(
      tpe := "text",
      placeholder := "Token",
      value := "",
      onInput.value --> sessionTokenFromClient,
    ),
    Button(
      ButtonStyle.Primary,
      "Finish Connection",
      cls := "w-full",
      disabled <-- Signal { sessionTokenFromClient.value.isBlank() },
      onClick.foreach(_ => confirmConnectionToClient()),
    ),
  )

  private def confirmConnectionToClient(): Unit = {
    connection.connector.set(PendingConnection.tokenAsSession(sessionTokenFromClient.now).session)
  }

  private def onConnected(ref: RemoteRef)(using state: Var[State]): Unit = {
    println(PendingConnection.tokenAsSession(sessionTokenFromClient.now).alias)
    jsImplicits.webrtc.setAlias(ref, PendingConnection.tokenAsSession(sessionTokenFromClient.now).alias)
    state.set(Init())
  }
}

class ManualConnectionDialog(using
    jsImplicits: JSImplicits,
)(private val state: Var[State] = Var(Init())) {

  private val mode = Var("host")
  ignoreDisconnectable(mode.observe(v => {
    if (v == "host") {
      state.set(Init())
    } else {
      state.set(ClientAskingForHostSessionToken())
    }
  }))

  def render: VNode = {
    div(
      div(
        cls := "flex rounded-xl mt-2 gap-1 text-center",
        input(
          tpe := "radio",
          name := "mode",
          idAttr := "hostMode",
          cls := "hidden peer/host",
          checked := true,
          value := "host",
          onInput.value --> mode,
        ),
        input(
          tpe := "radio",
          name := "mode",
          cls := "hidden peer/client",
          value := "client",
          idAttr := "clientMode",
          onInput.value --> mode,
        ),
        label(
          forId := "hostMode",
          cls := "dark:bg-gray-700 dark:text-gray-300 grow bg-white p-2 w-fill rounded-l-xl cursor-pointer uppercase font-bold text-xs text-purple-600 peer-checked/host:text-white peer-checked/host:bg-purple-600 shadow",
          "Host",
        ),
        label(
          forId := "clientMode",
          cls := "dark:bg-gray-700 dark:text-gray-300 grow bg-white p-2 w-fill rounded-r-xl cursor-pointer uppercase font-bold text-xs text-purple-600 peer-checked/client:text-white peer-checked/client:bg-purple-600 shadow",
          "Client",
        ),
      ),
      Signal { state.value.render(using state) },
    )
  }
}
