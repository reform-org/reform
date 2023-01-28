/*
Copyright 2022 The reform-org/reform contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
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
import webapp.services.Page

def navigationMenu(using services: Services)(classes: String) = {
  ul(
    tabIndex := 0,
    cls := classes,
    li(
      a(
        "Home",
        href := navigationLink(HomePage()),
      ),
    ),
    li(
      a(
        "Login",
        href := navigationLink(LoginPage()),
      ),
    ),
    li(
      a(
        "Projekte",
        href := navigationLink(ProjectsPage()),
      ),
    ),
    li(
      a(
        "Users",
        href := navigationLink(UsersPage()),
      ),
    ),
    li(
      a(
        "Paymentlevels",
        href := navigationLink(PaymentLevelsPage()),
      ),
    ),
    li(
      a(
        "SalaryChanges",
        href := navigationLink(SalaryChangesPage()),
      ),
    ),
    li(
      a(
        "Hiwis",
        href := navigationLink(HiwisPage()),
      ),
    ),
    li(
      a(
        "Supervisors",
        href := navigationLink(SupervisorsPage()),
      ),
    ),
    li(
      a(
        "ContractSchemas",
        href := navigationLink(ContractSchemasPage()),
      ),
    ),
    li(
      a(
        "WebRTC",
        href := navigationLink(WebRTCHandling()),
      ),
    ),
    li(
      i(
        s"${services.webrtc.registry.remotes.size} Connections",
      ),
    ),
  )
}

def navigationHeader(using services: Services) = {
  import svg.*
  div(
    cls := "navbar bg-base-300",
    div(
      cls := "navbar-start",
      div(
        cls := "dropdown",
        label(
          tabIndex := 0,
          idAttr := "dropdown-button",
          cls := "btn btn-ghost lg:hidden",
          svg(
            xmlns := "http://www.w3.org/2000/svg",
            cls := "h-5 w-5",
            fill := "none",
            viewBox := "0 0 24 24",
            stroke := "currentColor",
            path(
              VModifier.attr("stroke-linecap") := "round",
              VModifier.attr("stroke-linejoin") := "round",
              VModifier.attr("stroke-width") := "2",
              d := "M4 6h16M4 12h8m-8 6h16",
            ),
          ),
        ),
        navigationMenu("menu menu-compact dropdown-content mt-3 p-2 shadow bg-base-100 rounded-box w-52"),
      ),
      a(cls := "btn btn-ghost normal-case text-xl", "reform"),
    ),
    div(
      cls := "navbar-center hidden lg:flex",
      navigationMenu("menu menu-horizontal p-0"),
    ),
  )
}

def navigationLink(using services: Services)(page: Page): String = {
  onClick.foreach(e => {
    e.preventDefault()
    e.target.asInstanceOf[HTMLElement].blur()
    services.routing.to(page, true)
  })
  services.routing.linkPath(page)
}
