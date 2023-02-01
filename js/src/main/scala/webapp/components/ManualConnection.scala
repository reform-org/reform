package webapp.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import webapp.given
import webapp.pages.*
import org.scalajs.dom.document
import org.scalajs.dom.HTMLElement
import org.scalajs.dom.window

import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import org.scalajs.dom.{console, UIEvent}
import scala.scalajs.js
import webapp.webrtc.ConnectionInformation
import com.github.plokhotnyuk.jsoniter_scala.core.*
import webapp.webrtc.StoredConnectionInformation
import webapp.webrtc.PendingConnection
import webapp.webrtc.WebRTCService

private sealed trait State {
  def render(using state: Var[State], webrtc: WebRTCService): VNode
}

private case object Init extends State {
  private def initializeHostSession(using state: Var[State], webrtc: WebRTCService): Unit = {
    val pendingConnection =
      PendingConnection.webrtcIntermediate(WebRTC.offer(), alias.now)
    state.set(HostPending(pendingConnection))
  }

  private val alias = Var("")
  override def render(using state: Var[State], webrtc: WebRTCService): VNode = {
    div(
      cls := "form-control w-full text-sm",
      label(cls := "label", span(cls := "label-text text-slate-500", "What is your name?")),
      input(
        tpe := "text",
        placeholder := "Your name",
        cls := "input input-bordered w-full text-sm p-2 h-fit",
        onInput.value --> alias,
        value := "",
      ),
      button(
        cls := "btn btn-active bg-purple-600 p-2 h-fit min-h-10 mt-2 border-0 hover:bg-purple-600 w-full",
        "Create Invitation",
        disabled <-- alias.map(_.isBlank()),
        onClick.foreach(_ => initializeHostSession),
      ),
    )
  }
}

private case class ClientAskingForHostSessionToken() extends State {
  private val sessionToken = Var("")
  private val alias = Var("")
  override def render(using state: Var[State], webrtc: WebRTCService): VNode = div(
    cls := "p1",
    label(cls := "label", span(cls := "label-text text-slate-500", "What is your name?")),
    input(
      tpe := "text",
      placeholder := "Your name",
      cls := "input input-bordered w-full text-sm p-2 h-fit",
      onInput.value --> alias,
      value := "",
    ),
    label(cls := "label", span(cls := "label-text text-slate-500", "Please enter the code your peer has provided:")),
    input(
      tpe := "text",
      placeholder := "Token",
      cls := "input input-bordered w-full text-sm p-2 h-fit",
      value := "",
      onInput.value --> sessionToken,
    ),
    button(
      cls := "btn btn-active bg-purple-600 p-2 h-fit min-h-10 mt-2 border-0 hover:bg-purple-600 w-full",
      "Connect",
      disabled <-- alias.map(a => sessionToken.map(_.isBlank() || a.isBlank())).flatten,
      onClick.foreach(_ => connectToHost),
    ),
  )

  private def connectToHost(using state: Var[State])(using webrtc: WebRTCService): Unit = {
    val connection = PendingConnection.webrtcIntermediate(WebRTC.answer(), alias.now)
    connection.connector.set(PendingConnection.tokenAsSession(sessionToken.now).session)
    state.set(ClientWaitingForHostConfirmation(connection, alias.now))
  }
}

private case class ClientWaitingForHostConfirmation(connection: PendingConnection, alias: String)(using
    state: Var[State],
    webrtc: WebRTCService,
) extends State {
  webrtc
    .registerConnection(connection.connector, connection.session.map(i => i.alias), "manual", connection.connection)
    .foreach(_ => onConnected())

  override def render(using state: Var[State], webrtc: WebRTCService): VNode = div(
    cls := "p-1",
    span(
      cls := "label-text text-slate-500",
      "Please share the code with the peer that invited you to finish the connection.",
    ),
    connection.session.map(session =>
      div(
        cls := "flex gap-1 mt-2",
        button(
          data.token := PendingConnection.sessionAsToken(session),
          cls := "w-fit h-fit btn btn-square rounded-xl bg-purple-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
          Icons.clipboard("w-6 h-6", "white", "group-hover:stroke-purple-600"),
          onClick.foreach(_ => window.navigator.clipboard.writeText(PendingConnection.sessionAsToken(session))),
        ),
        a(
          cls := "w-fit h-fit btn btn-square rounded-xl bg-purple-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
          Icons.mail("w-6 h-6", "white", "group-hover:stroke-purple-600"),
          href := s"mailto:?subject=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${PendingConnection
              .sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
        a(
          cls := "w-fit h-fit btn btn-square rounded-xl bg-green-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
          Icons.whatsapp("w-6 h-6 group-hover:fill-green-600", "white"),
          href := s"whatsapp://send?text=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${PendingConnection
              .sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
      ),
    ),
  )

  private def onConnected()(using state: Var[State]): Unit = {
    state.set(ClientAskingForHostSessionToken())

  }
}

private case class HostPending(connection: PendingConnection)(using state: Var[State], webrtc: WebRTCService)
    extends State {
  private val sessionTokenFromClient = Var("")

  webrtc
    .registerConnection(connection.connector, connection.session.map(i => i.alias), "manual", connection.connection)
    .foreach(_ => onConnected())

  override def render(using state: Var[State], webrtc: WebRTCService): VNode = div(
    cls := "p-1",
    span(
      cls := "label-text text-slate-500",
      "Please share the Invitation with one peer. The peer will respond with an code which finishes the connection.",
    ),
    connection.session.map(session =>
      div(
        cls := "flex gap-1 mt-2",
        button(
          data.token := PendingConnection.sessionAsToken(session),
          cls := "w-fit h-fit btn btn-square rounded-xl bg-purple-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
          Icons.clipboard("w-6 h-6", "white", "group-hover:stroke-purple-600"),
          onClick.foreach(_ => window.navigator.clipboard.writeText(PendingConnection.sessionAsToken(session))),
        ),
        a(
          cls := "w-fit h-fit btn btn-square rounded-xl bg-purple-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
          Icons.mail("w-6 h-6", "white", "group-hover:stroke-purple-600"),
          href := s"mailto:?subject=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${PendingConnection
              .sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
        a(
          cls := "w-fit h-fit btn btn-square rounded-xl bg-green-600 p-2 min-h-10 border-0 hover:bg-white shadow-md group",
          Icons.whatsapp("w-6 h-6 group-hover:fill-green-600", "white"),
          href := s"whatsapp://send?text=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${PendingConnection
              .sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
      ),
    ),
    label(cls := "label", span(cls := "label-text text-slate-500", "Please enter the code your peer has provided:")),
    input(
      tpe := "text",
      placeholder := "Token",
      cls := "input input-bordered w-full text-sm p-2 h-fit",
      value := "",
      onInput.value --> sessionTokenFromClient,
    ),
    button(
      cls := "btn btn-active bg-purple-600 p-2 h-fit min-h-10 mt-2 border-0 hover:bg-purple-600 w-full",
      "Finish Connection",
      // disabled := true,
      onClick.foreach(_ => confirmConnectionToClient()),
    ),
  )

  private def confirmConnectionToClient(): Unit = {
    connection.connector.set(PendingConnection.tokenAsSession(sessionTokenFromClient.now).session)
  }

  private def onConnected()(using state: Var[State]): Unit = {
    state.set(Init)
  }
}

class ManualConnectionDialog(private val state: Var[State] = Var(Init)) {

  private val mode = Var("host")
  mode.observe(v => {
    if (v == "host") {
      state.set(Init)
    } else {
      state.set(ClientAskingForHostSessionToken())
    }
  })

  def render(using webrtc: WebRTCService): VNode = {
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
          cls := "grow bg-white p-2 w-fill rounded-l-xl cursor-pointer uppercase font-bold text-xs text-purple-600 peer-checked/host:text-white peer-checked/host:bg-purple-600 shadow",
          "Host",
        ),
        label(
          forId := "clientMode",
          cls := "grow bg-white p-2 w-fill rounded-r-xl cursor-pointer uppercase font-bold text-xs text-purple-600 peer-checked/client:text-white peer-checked/client:bg-purple-600 shadow",
          "Client",
        ),
      ),
      state.map(_.render(using state, webrtc)),
    )
  }
}
