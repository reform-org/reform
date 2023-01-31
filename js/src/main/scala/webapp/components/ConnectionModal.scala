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
import webapp.utils.Base64
import webapp.webrtc.StoredConnectionInformation
import webapp.services.DiscoveryService
import webapp.webrtc.WebRTCService

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

class ConnectionModal(using webrtc: WebRTCService, discovery: DiscoveryService) {
  discovery.login(new discovery.LoginInfo("Lukas", "test"))
  discovery.connect()

  def render: VNode = {
    ul(
      tabIndex := 0,
      cls := "p-2 shadow-xl menu menu-compact bg-base-100 w-52",
      h2(
        "Connections",
        cls := "font-bold text-lg p-2"
      ),
      webrtc.connections.map(_.map(ref => {
        val info = webrtc.getInformation(ref)
        connectionRow(info.alias, info.source, ref)
      })),
      webrtc.connections.map(connections => {
        var emptyState: VNode = div()
        if (connections.size == 0) {
          emptyState = div(
            cls := "flex flex-col items-center mt-4 mb-4",
            Icons.ghost("w-14 h-14 mb-2"),
            i("It's quiet for now..."),
          )
        }
        emptyState
      }),
      div(cls := "divider uppercase text-slate-300 font-bold text-xs mb-2", "Auto"),
      // li(
      //   offlineBanner,
      // ),
      li(
        onlineBanner,
      ),
      label(
        cls := "label cursor-pointer",
        span(cls := "label-text", "Autoconnect"),
        input(
          tpe := "checkbox",
          cls := "toggle",
          checked := Settings.get[Boolean]("autoconnect").getOrElse(false),
          onClick.foreach(e =>
            discovery.setAutoconnect(e.target.asInstanceOf[dom.HTMLInputElement].checked),
          ),
        ),
      ),
      div(cls := "divider uppercase text-slate-300 font-bold text-xs mb-0", "Manual"),
      ManualConnectionDialog().render(),
    )

  }
}
