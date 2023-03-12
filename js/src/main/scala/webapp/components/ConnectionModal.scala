package webapp.components

import org.scalajs.dom
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.console
import org.scalajs.dom.document
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.given
import webapp.services.DiscoveryService
import webapp.webrtc.WebRTCService
import webapp.services.Toaster

import webapp.given_ExecutionContext
import scala.util.Failure
import scala.util.Success
import webapp.components.common.*
import webapp.utils.Futures.*
import scala.annotation.nowarn

class ConnectionModal(using webrtc: WebRTCService, discovery: DiscoveryService, toaster: Toaster) {
  val offlineBanner = {
    div(
      cls := "bg-amber-100 flex flex-col items-center",
      icons.Reload(
        cls := "h-8 w-8 animate-reload text-amber-600",
        onClick.foreach(e => {
          if (Settings.get[Boolean]("autoconnect").getOrElse(true)) {
            e.target.classList.add("animate-spin")
            discovery
              .connect(true, true)
              .transform(res => {
                window.setTimeout(() => e.target.classList.remove("animate-spin"), 1000): @nowarn
                res
              })
              .toastOnError()
          }
        }),
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
        "You are connected to the discovery server as ",
        i(Signal { discovery.token.value.map(t => discovery.decodeToken(t).username) }),
        "!",
      ),
    )
  }

  def render(using toaster: Toaster): VNode = {
    ul(
      tabIndex := 0,
      cls := "p-2 shadow-xl menu menu-compact bg-base-100 w-52 dark:bg-gray-600 dark:text-gray-200",
      h2(
        "Connections",
        cls := "font-bold text-lg p-2",
      ),
      webrtc.connections.map(_.map(ref => {
        val info = webrtc.getInformation(ref)
        connectionRow(info.alias, info.source, info.uuid, info.displayId, ref)
      })),
      webrtc.connections.map(connections => {
        var emptyState: VNode = div()
        if (connections.size == 0) {
          emptyState = div(
            cls := "flex flex-col items-center mt-4 mb-4",
            icons.Ghost(cls := "w-14 h-14 mb-2"),
            i("No other clients available"),
          )
        }
        emptyState
      }),
      div(
        cls := "divider uppercase text-slate-300 font-bold text-xs mb-2 after:dark:bg-gray-300 before:dark:bg-gray-300",
        "Auto",
      ), {
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
      Signal {
        val connections = webrtc.connections.value.map(webrtc.getInformation(_).uuid)
        val availableConnections = discovery.availableConnections.value.filter(p => !connections.contains(p.uuid))
        availableConnections.map(connection => availableConnectionRow(connection))
      },
      label(
        cls := "label cursor-pointer",
        span(cls := "label-text dark:text-gray-300", "Autoconnect"),
        Checkbox(
          CheckboxStyle.Primary,
          cls := "checkbox-sm",
          checked := Settings.get[Boolean]("autoconnect").getOrElse(true),
          onClick.foreach(e => discovery.setAutoconnect(e.target.asInstanceOf[dom.HTMLInputElement].checked)),
        ),
      ),
      div(
        cls := "divider uppercase text-slate-300 font-bold text-xs mb-0  after:dark:bg-gray-300 before:dark:bg-gray-300",
        "Manual",
      ),
      ManualConnectionDialog().render(),
    )

  }
}

class Login() {
  private val username = Var("")
  private val password = Var("")

  def render(using discovery: DiscoveryService, webrtc: WebRTCService, toaster: Toaster): VNode = {
    div(
      discovery.token
        .map(token =>
          if (discovery.tokenIsValid(token))
            Button(
              ButtonStyle.Primary,
              "Logout",
              onClick.foreach(_ => {
                discovery.logout()
              }),
              cls := "w-full mt-2",
            )
          else
            div(
              cls := "form-control w-full text-sm",
              label(cls := "label", span(cls := "label-text text-slate-500 dark:text-gray-300", "Username")),
              input(
                tpe := "text",
                placeholder := "Username",
                cls := "input input-bordered w-full text-sm p-2 h-fit dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white",
                idAttr := "login-username",
                onInput.value --> username,
                value := "",
              ),
              label(cls := "label", span(cls := "label-text text-slate-500 dark:text-gray-300", "Password")),
              input(
                tpe := "password",
                placeholder := "Password",
                idAttr := "login-password",
                cls := "input input-bordered w-full text-sm p-2 h-fit dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white",
                onInput.value --> password,
                value := "",
              ),
              Button(
                ButtonStyle.Primary,
                "Login",
                cls := "w-full mt-2",
                disabled <-- username.map(s => s.isBlank()), // || password.map(s => s.isBlank())}
                onClick
                  .foreach(_ =>
                    discovery
                      .login(new discovery.LoginInfo(username.now, password.now))
                      .onComplete(result => {
                        result match {
                          case Failure(exception: discovery.LoginException) => {
                            exception.fields.foreach(field => {
                              val input = document.querySelector(s"#login-$field").asInstanceOf[HTMLInputElement]
                              input.setCustomValidity(exception.message)
                              input.reportValidity()
                            })
                          }
                          case Failure(_) => console.log("some login error has happened")
                          case Success(value) => {
                            discovery.setAutoconnect(true)
                          }
                        }
                      }),
                  ),
              ),
            ),
        ),
    )
  }
}
