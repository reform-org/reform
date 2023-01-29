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

class ConnectionModal(using services: Services) {
  services.discovery.login(new services.discovery.LoginInfo("Lukas", "test"))
  services.discovery.connect(using services)

  def render(using services: Services): VNode = {
    div(
      cls := "flex-none gap-2",
      div(
        cls := "dropdown dropdown-end h-full",
        idAttr := "connection-modal-content",
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
          cls := "mt-3 p-2 shadow-xl menu menu-compact dropdown-content bg-base-100 rounded-box w-52",
          services.webrtc.connections.map(_.map(ref => {
            val info = services.webrtc.getInformation(ref)
            connectionRow(info.alias, info.source, ref)
          })),
          services.webrtc.connections.map(connections => {
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
          div(cls := "divider uppercase text-slate-300 font-bold text-xs mb-0", "Manual"),
          ManualConnectionDialog().render,
        ),
      ),
    )
  }
}
