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

import org.scalajs.dom.HTMLElement
import outwatch.*
import outwatch.dsl.*
import webapp.*
import webapp.given
import webapp.pages.*
import webapp.services.DiscoveryService
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService

def navigationMenu(using routing: RoutingService, repositories: Repositories, webrtc: WebRTCService)(
    classes: String,
) = {
  ul(
    tabIndex := 0,
    cls := classes,
    li(
      navigationLink(ProjectsPage(), "Projects"),
    ),
    li(
      navigationLink(UsersPage(), "Users"),
    ),
    li(
      a(
        cls := "btn btn-ghost normal-case	font-normal rounded-md	",
        "Settings",
        Icons.expand("w-4 h-4"),
      ),
      ul(
        cls := "p-2 bg-base-100 focus:bg-slate-200 z-10 shadow-lg rounded-md",
        li(
          navigationLink(PaymentLevelsPage(), "Payment levels"),
        ),
        li(
          navigationLink(SalaryChangesPage(), "Salary changes"),
        ),
      ),
    ),
    li(
      navigationLink(HiwisPage(), "Hiwis"),
    ),
    li(
      navigationLink(SupervisorsPage(), "Supervisors"),
    ),
    li(
      navigationLink(ContractSchemasPage(), "Contract schemas"),
    ),
    li(
      navigationLink(RequiredDocumentsPage(), "Required documents"),
    ),
  )
}

def navigationHeader(
    content: VNode,
)(using routing: RoutingService, repositories: Repositories, webrtc: WebRTCService, discovery: DiscoveryService) = {
  div(
    cls := "drawer drawer-end",
    input(idAttr := "connection-drawer", tpe := "checkbox", cls := "drawer-toggle"),
    div(
      cls := "drawer-content flex flex-col",
      div(
        cls := "navbar bg-base-100 shadow",
        div(
          cls := "flex-none",
          div(
            cls := "dropdown",
            label(
              tabIndex := 0,
              idAttr := "dropdown-button",
              cls := "btn btn-ghost lg:hidden",
              Icons.hamburger("h-5 w-5"),
            ),
            navigationMenu("menu menu-compact dropdown-content mt-3 p-2 shadow bg-base-100 rounded-box w-52"),
          ),
        ),
        div(
          cls := "flex-1",
          a(
            Icons.reform(),
            cls := "btn btn-ghost normal-case text-xl",
            href := "/",
            onClick.foreach(e => {
              e.preventDefault()
              e.target.asInstanceOf[HTMLElement].blur()
              routing.to(HomePage(), true)
            }),
          ),
        ),
        div(
          cls := "navbar-center hidden lg:flex",
          navigationMenu("menu menu-horizontal p-0"),
        ),
        div(
          cls := "flex-none",
          label(
            forId := "connection-drawer",
            cls := "btn btn-ghost",
            div(
              cls := "indicator",
              Icons.connections("h-6 w-6", "#000"),
              span(
                cls := "badge badge-sm indicator-item",
                webrtc.connections.map(_.size),
              ),
            ),
          ),
        ),
      ),
      content,
    ),
    div(
      cls := "drawer-side",
      label(forId := "connection-drawer", cls := "drawer-overlay"),
      ConnectionModal().render,
    ),
  )
}
