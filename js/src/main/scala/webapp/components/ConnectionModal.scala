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
import scala.util.Failure
import scala.util.Success
import org.scalajs.dom.{window, HTMLInputElement}

class ConnectionModal(using webrtc: WebRTCService, discovery: DiscoveryService) {
  if (discovery.tokenIsValid(discovery.getToken())) discovery.connect()

  val offlineBanner = {
    div(
      cls := "bg-amber-100 flex flex-col	items-center	",
      Icons.reload(
        "h-8 w-8 animate-reload",
        "#D97706",
        e => {
          e.target.classList.add("animate-spin")
          discovery
            .connect(true)
            .onComplete(event => {
              window.setTimeout(() => e.target.classList.remove("animate-spin"), 1000)
            })
        },
      ),
      span(
        cls := "text-amber-600 font-semibold text-center",
        "You are not connected to the discovery server!",
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

  def render: VNode = {
    ul(
      tabIndex := 0,
      cls := "p-2 shadow-xl menu menu-compact bg-base-100 w-52",
      h2(
        "Connections",
        cls := "font-bold text-lg p-2",
      ),
      webrtc.connections.map(_.map(ref => {
        val info = webrtc.getInformation(ref)
        connectionRow(info.alias, info.source, info.uuid, ref)
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
      div(cls := "divider uppercase text-slate-300 font-bold text-xs mb-2", "Auto"), {
        discovery.online.map(online => {
          if (online) {
            li(
              onlineBanner,
            )
          } else {
            li(
              offlineBanner,
            )
          }
        })
      },
      Login().render,
      discovery.availableConnections.map(_.map(connection => availableConnectionRow(connection))),
      label(
        cls := "label cursor-pointer",
        span(cls := "label-text", "Autoconnect"),
        input(
          tpe := "checkbox",
          cls := "toggle toggle-sm bg-purple-600 border-purple-600",
          checked := Settings.get[Boolean]("autoconnect").getOrElse(false),
          onClick.foreach(e => discovery.setAutoconnect(e.target.asInstanceOf[dom.HTMLInputElement].checked)),
        ),
      ),
      div(cls := "divider uppercase text-slate-300 font-bold text-xs mb-0", "Manual"),
      ManualConnectionDialog().render(),
    )

  }
}

class Login() {
  private val username = Var("")
  private val password = Var("")

  def render(using discovery: DiscoveryService, webrtc: WebRTCService): VNode = {
    div(
      discovery
        .getTokenSignal()
        .map(token =>
          if (discovery.tokenIsValid(token))
            button(
              cls := "btn btn-active bg-purple-600 p-2 h-fit min-h-10 mt-2 border-0 hover:bg-purple-600 w-full",
              "Logout",
              onClick.foreach(_ => {
                discovery.logout()
              }),
            )
          else
            div(
              cls := "form-control w-full text-sm",
              label(cls := "label", span(cls := "label-text text-slate-500", "Username")),
              input(
                tpe := "text",
                placeholder := "Username",
                cls := "input input-bordered w-full text-sm p-2 h-fit",
                idAttr := "login-username",
                onInput.value --> username,
                value := "",
              ),
              label(cls := "label", span(cls := "label-text text-slate-500", "Password")),
              input(
                tpe := "password",
                placeholder := "Password",
                idAttr := "login-password",
                cls := "input input-bordered w-full text-sm p-2 h-fit",
                onInput.value --> password,
                value := "",
              ),
              button(
                cls := "btn btn-active bg-purple-600 p-2 h-fit min-h-10 mt-2 border-0 hover:bg-purple-600 w-full",
                "Login",
                disabled <-- username.map(s => s.isBlank()), // || password.map(s => s.isBlank())}
                onClick
                  .foreach(_ =>
                    discovery
                      .login(new discovery.LoginInfo(username.now, password.now))
                      .onComplete(result => {
                        result match {
                          case Failure(exception: discovery.LoginException) => {
                            console.log(exception.message)
                            console.log(exception.fields)
                            exception.fields.foreach(field => {
                              val input = document.querySelector(s"#login-$field").asInstanceOf[HTMLInputElement]
                              input.setCustomValidity(exception.message)
                              input.reportValidity()
                            })
                          }
                          case Failure(_)     => console.log("some login error has happened")
                          case Success(value) => {}
                        }
                      }),
                  ),
              ),
            ),
        ),
    )
  }
}
