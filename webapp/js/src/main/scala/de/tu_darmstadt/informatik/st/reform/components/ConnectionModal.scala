package de.tu_darmstadt.informatik.st.reform.components

import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.given
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.services.LoginException
import de.tu_darmstadt.informatik.st.reform.services.LoginInfo
import de.tu_darmstadt.informatik.st.reform.utils.Futures.*
import org.scalajs.dom
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.console
import org.scalajs.dom.document
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

import scala.util.Failure
import scala.util.Success

class ConnectionModal(using jsImplicits: JSImplicits) {
  private val offlineBanner: VNode = {
    div(
      cls := "bg-amber-100 flex flex-col items-center",
      icons.Reload(
        cls := "h-8 w-8 animate-reload text-amber-600",
        onClick.foreach(e =>
          Signal {
            if (autoconnect.value) {
              e.target.classList.add("animate-spin")
              jsImplicits.discovery
                .connect(true, true)
                .transform(res => {
                  window.setTimeout(() => e.target.classList.remove("animate-spin"), 1000)
                  res
                })
                .toastOnError()
            }
          },
        ),
      ),
      span(
        cls := "text-amber-600 font-semibold text-center",
        "You are not connected to the discovery server!",
      ),
    )
  }

  private val onlineBanner: VNode = {
    div(
      cls := "bg-green-100 flex flex-col	items-center",
      span(
        cls := "text-green-600 font-semibold text-center",
        "You are connected to the discovery server as ",
        i(Signal { jsImplicits.discovery.token.value.map(t => jsImplicits.discovery.decodeToken(t).username) }),
        "!",
      ),
    )
  }

  def render: VMod = {
    ul(
      tabIndex := 0,
      cls := "p-2 shadow-xl menu menu-compact bg-base-100 w-52 dark:bg-gray-600 dark:text-gray-200",
      h2(
        "Connections",
        cls := "font-bold text-lg p-2",
      ),
      Signal {
        jsImplicits.webrtc.connections.value.map(ref => {
          val info = jsImplicits.webrtc.getInformation(ref)
          connectionRow(info.alias, info.source, info.uuid, info.displayId, ref)
        })
      },
      Signal {
        var emptyState: VNode = div()
        if (jsImplicits.webrtc.connections.value.isEmpty) {
          emptyState = div(
            cls := "flex flex-col items-center mt-4 mb-4",
            icons.Ghost(cls := "w-14 h-14 mb-2"),
            i("No other clients available"),
          )
        }
        emptyState
      },
      div(
        cls := "divider uppercase text-slate-300 font-bold text-xs mb-2 after:dark:bg-gray-300 before:dark:bg-gray-300",
        "Auto",
      ), {
        Signal {
          if (jsImplicits.discovery.online.value) {
            li(
              onlineBanner,
            )
          } else {
            li(
              offlineBanner,
            )
          }
        }
      },
      Login().render,
      Signal {
        val connections = jsImplicits.webrtc.connections.value.map(jsImplicits.webrtc.getInformation(_).uuid)
        val availableConnections =
          jsImplicits.discovery.availableConnections.value.filter(p => !connections.contains(p.uuid))
        availableConnections.map(connection => availableConnectionRow(connection))
      },
      label(
        cls := "label cursor-pointer",
        span(cls := "label-text dark:text-gray-300", "Autoconnect"),
        Checkbox(
          CheckboxStyle.Primary,
          cls := "checkbox-sm",
          checked <-- autoconnect,
          onClick.foreach(e =>
            jsImplicits.discovery.setAutoconnect(e.target.asInstanceOf[dom.HTMLInputElement].checked),
          ),
        ),
      ),
      div(
        cls := "divider uppercase text-slate-300 font-bold text-xs mb-0  after:dark:bg-gray-300 before:dark:bg-gray-300",
        "Manual",
      ),
      ManualConnectionDialog().render,
    )

  }
}

class Login(using jsImplicits: JSImplicits) {
  private val username = Var("")
  private val password = Var("")

  def render: VMod = {
    div(
      Signal {
        if (jsImplicits.discovery.tokenIsValid(jsImplicits.discovery.token.value))
          Button(
            ButtonStyle.Primary,
            "Logout",
            onClick.foreach(_ => {
              jsImplicits.discovery.logout()
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
              disabled <-- Signal { username.value.isBlank || password.value.isBlank },
              onClick
                .foreach(_ =>
                  jsImplicits.discovery
                    .login(new LoginInfo(username.now, password.now))
                    .onComplete {
                      case Failure(exception: LoginException) =>
                        exception.fields.foreach(field => {
                          val input = document.querySelector(s"#login-$field").asInstanceOf[HTMLInputElement]
                          input.setCustomValidity(exception.message)
                          input.reportValidity()
                        })
                      case Failure(_) => console.log("some login error has happened")
                      case Success(value) =>
                        jsImplicits.discovery.setAutoconnect(true)
                    },
                ),
            ),
          )
      },
    )
  }
}
