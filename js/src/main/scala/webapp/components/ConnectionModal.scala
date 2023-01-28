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
import webapp.webrtc.ConnectionInformation
import com.github.plokhotnyuk.jsoniter_scala.core.*
import webapp.utils.Base64
import webapp.webrtc.StoredConnectionInformation

private case class PendingConnection(connector: WebRTC.Connector, session: Future[ConnectionInformation])

private def webrtcIntermediate(cf: ConnectorFactory, alias: String) = {
  val p = Promise[ConnectionInformation]()
  val answer = cf.complete(s => p.success(new ConnectionInformation(s, alias)))
  PendingConnection(answer, p.future)
}

private val codec: JsonValueCodec[ConnectionInformation] = JsonCodecMaker.make

private def sessionAsToken(s: ConnectionInformation) = Base64.encode(writeToString(s)(codec))

private def tokenAsSession(s: String) =
  readFromString(Base64.decode(s))(codec) // readFromString(Base64.decode(s))(codec)

private sealed trait State {
  def render(using state: Var[State], services: Services): VNode
}

private case object Init extends State {
  private def initializeHostSession(using state: Var[State], services: Services): Unit = {
    val pendingConnection =
      webrtcIntermediate(WebRTC.offer(), alias.now)

    state.set(HostPending(pendingConnection))

  }

  private val alias = Var("")
  override def render(using state: Var[State], services: Services): VNode = {
    div(
      cls := "form-control w-full text-sm",
      label(cls := "label", span(cls := "label-text text-slate-500", "What is your name?")),
      input(
        tpe := "text",
        placeholder := "Type here",
        cls := "input input-bordered w-full text-sm p-2 h-fit",
        onInput.value --> alias,
        value := "",
      ),
      button(
        cls := "btn btn-active bg-purple-600 p-2 h-fit min-h-10 mt-2 border-0 hover:bg-purple-600 w-full",
        "Create Invitation",
        // disabled := alias.map(_.isBlank()),
        onClick.foreach(_ => initializeHostSession),
      ),
    )
  }
}

private case class ClientAskingForHostSessionToken() extends State {
  private val sessionToken = Var("")
  private val alias = Var("")
  override def render(using state: Var[State], services: Services): VNode = div(
    cls := "p1",
    label(cls := "label", span(cls := "label-text text-slate-500", "What is your name?")),
    input(
      tpe := "text",
      placeholder := "Type here",
      cls := "input input-bordered w-full text-sm p-2 h-fit",
      onInput.value --> alias,
      value := "",
    ),
    label(cls := "label", span(cls := "label-text text-slate-500", "Please enter the code your peer has provided:")),
    input(
      tpe := "text",
      placeholder := "Type here",
      cls := "input input-bordered w-full text-sm p-2 h-fit",
      value := "",
      onInput.value --> sessionToken,
    ),
    button(
      cls := "btn btn-active bg-purple-600 p-2 h-fit min-h-10 mt-2 border-0 hover:bg-purple-600 w-full",
      "Connect",
      // disabled := true,
      onClick.foreach(_ => connectToHost),
    ),
  )

  private def connectToHost(using state: Var[State], services: Services): Unit = {
    val connection = webrtcIntermediate(WebRTC.answer(), alias.now)
    connection.connector.set(tokenAsSession(sessionToken.now).session)
    state.set(ClientWaitingForHostConfirmation(connection, alias.now))

  }
}

private case class ClientWaitingForHostConfirmation(connection: PendingConnection, alias: String)(using
    state: Var[State],
    services: Services,
) extends State {
  services.webrtc
    .registerConnection(connection.connector, connection.session.map(i => i.alias), "manual")
    .foreach(_ => onConnected())

  override def render(using state: Var[State], services: Services): VNode = div(
    cls := "p-1",
    span(
      cls := "label-text text-slate-500",
      "Please share the code with the peer that invited you to finish the connection.",
    ),
    div(
      cls := "flex gap-1 mt-2 justify-center",
      button(
        cls := "w-fit h-fit btn btn-circle bg-slate-800 p-2 min-h-10 border-0",
        Icons.clipboard("w-6 h-6", "white"),
        onClick.foreach(_ => connection.session.map(sessionAsToken).map(s => window.navigator.clipboard.writeText(s))),
      ),
      connection.session.map(session =>
        a(
          cls := "w-fit h-fit btn btn-circle bg-slate-800 p-2 min-h-10 border-0",
          Icons.mail("w-6 h-6", "white"),
          href := s"mailto:?subject=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
      ),
      connection.session.map(session =>
        a(
          cls := "w-fit h-fit btn btn-circle bg-green-600 p-2 min-h-10 border-0",
          Icons.whatsapp("w-6 h-6", "white"),
          href := s"whatsapp://send?text=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
      ),
    ),
  )

  private def onConnected()(using state: Var[State]): Unit = {
    state.set(ClientAskingForHostSessionToken())

  }
}

private case class HostPending(connection: PendingConnection)(using state: Var[State], services: Services)
    extends State {
  private val sessionTokenFromClient = Var("")

  services.webrtc
    .registerConnection(connection.connector, connection.session.map(i => i.alias), "manual")
    .foreach(_ => onConnected())

  override def render(using state: Var[State], services: Services): VNode = div(
    cls := "p-1",
    span(
      cls := "label-text text-slate-500",
      "Please share the Invitation with one peer. The peer will respond with an code which finishes the connection.",
    ),
    div(
      cls := "flex gap-1 mt-2 justify-center",
      button(
        cls := "w-fit h-fit btn btn-circle bg-slate-800 p-2 min-h-10 border-0",
        Icons.clipboard("w-6 h-6", "white"),
        onClick.foreach(_ => connection.session.map(sessionAsToken).map(s => window.navigator.clipboard.writeText(s))),
      ),
      connection.session.map(session =>
        a(
          cls := "w-fit h-fit btn btn-circle bg-slate-800 p-2 min-h-10 border-0",
          Icons.mail("w-6 h-6", "white"),
          href := s"mailto:?subject=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
      ),
      connection.session.map(session =>
        a(
          cls := "w-fit h-fit btn btn-circle bg-green-600 p-2 min-h-10 border-0",
          Icons.whatsapp("w-6 h-6", "white"),
          href := s"whatsapp://send?text=REForm%20Invitation&body=Hey%2C%0A${session.alias}%20would%20like%20you%20to%20accept%20the%20following%20invitation%20to%20connect%20to%20REForm%20by%20opening%20the%20following%20URL%20in%20your%20Browser%3A%0A%0A${sessionAsToken(session)}%2F%0A%0ASee%20you%20there%2C%0AThe%20REForm%20Team",
        ),
      ),
    ),
    label(cls := "label", span(cls := "label-text text-slate-500", "Please enter the code your peer has provided:")),
    input(
      tpe := "text",
      placeholder := "Type here",
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
    connection.connector.set(tokenAsSession(sessionTokenFromClient.now).session)
  }

  private def onConnected()(using state: Var[State]): Unit = {
    state.set(Init)

  }
}

private case object Connected extends State {
  def render(using state: Var[State], services: Services): VNode = h2(cls := "w-full text-2xl text-center", "Connected")
}

val offlineBanner = {
  div(
    cls := "bg-amber-100 flex flex-col	items-center	",
    Icons.reload("h-8 w-8 hover:animate-spin", "#D97706"),
    span(
      cls := "text-amber-600 font-semibold text-center",
      "You are not connected to the internet!",
    ),
  )
}

val onlineBanner = {
  div(
    cls := "bg-green-100 flex flex-col	items-center",
    span(
      cls := "text-green-600 font-semibold text-center",
      "You are connected to the discovery server!",
    ),
  )
}

class ConnectionModal(private val state: Var[State] = Var(Init)) {

  private val mode = Var("host")
  mode.observe(v => {
    if (v == "host") {
      state.set(Init)
    } else {
      state.set(ClientAskingForHostSessionToken())
    }
  })

  def render(using services: Services): VNode = {
    div(
      cls := "flex-none gap-2",
      div(
        cls := "dropdown dropdown-end h-full",
        label(
          tabIndex := 0,
          cls := "btn btn-ghost",
          div(
            cls := "indicator",
            Icons.connections("h-6 w-6", "#000"),
            span(
              cls := "badge badge-sm indicator-item",
              services.webrtc.connections.map(_.size),
            ),
          ),
        ),
        ul(
          tabIndex := 0,
          idAttr := "connection-modal-content",
          cls := "mt-3 p-2 shadow-xl menu menu-compact dropdown-content bg-base-100 rounded-box w-52",
            services.webrtc.connections.map(_.map(ref => {
              val info = services.webrtc.getInformation(ref)
              connectionRow(info.alias, info.source, ref)
            })),
          div(cls := "divider uppercase text-slate-300 font-bold text-xs mb-2", "Auto"),
          // li(
          //   offlineBanner,
          // ),
          li(
            onlineBanner,
          ),
          div(cls := "divider uppercase text-slate-300 font-bold text-xs mb-0", "Manual"),
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
          state.map(_.render(using state)),
          //   li(
          //     a(
          //       services.webrtc.connections.map(_.size),
          //       " Connections",
          //     ),
          //   ),
        ),
      ),
    )
  }
}
