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
package de.tu_darmstadt.informatik.st.reform.components

import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.given
import de.tu_darmstadt.informatik.st.reform.pages.DocumentsPage
import de.tu_darmstadt.informatik.st.reform.pages.*
import org.scalajs.dom.HTMLElement
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

def navigationMenu(using
    jsImplicits: JSImplicits,
)(
    classes: String,
) = {
  ul(
    tabIndex := 0,
    cls := classes,
    li(
      navigationLink(ProjectsPage(), "Projects"),
    ),
    li(
      navigationLink(SupervisorsPage(), "Supervisors"),
    ),
    li(
      navigationLink(HiwisPage(), "Hiwis"),
    ),
    li(
      navigationLink(ContractsPage(), "Contracts"),
    ),
    li(
      navigationLink(ContractDraftsPage(), "Contract Drafts"),
    ),
    li(
      a(
        cls := "btn btn-ghost active:!text-sm active:!text-gray-800 dark:active:!text-gray-200 normal-case	font-normal rounded-md hover:bg-slate-100 dark:hover:bg-gray-800/50",
        "Setup",
        icons.Expand(cls := "w-4 h-4"),
      ),
      ul(
        cls := "p-2 bg-base-100 focus:bg-slate-200 shadow-lg rounded-md !z-[10] dark:bg-gray-700",
        li(
          navigationLink(PaymentLevelsPage(), "Payment Levels"),
        ),
        li(
          navigationLink(SalaryChangesPage(), "Salary Changes"),
        ),
        li(
          navigationLink(ContractSchemasPage(), "Contract Schemas"),
        ),
        li(
          navigationLink(DocumentsPage(), "Documents"),
        ),
      ),
    ),
    li(
      navigationIconLink(SettingsPage(), icons.Settings(cls := "h-5 w-5")),
    ),
  )
}

def navigationHeader(
    content: VMod*,
)(using
    jsImplicits: JSImplicits,
) = {
  val dropdownOpen = Var(false)
  div(
    cls := "drawer drawer-end",
    input(idAttr := "connection-drawer", tpe := "checkbox", cls := "drawer-toggle"),
    div(
      cls := "drawer-content h-full flex flex-col dark:bg-gray-600 dark:text-gray-200",
      div(
        cls := "navbar bg-base-100 shadow dark:bg-gray-700 fixed z-[10000]",
        div(
          cls := "flex-none",
          div(
            cls := "dropdown",
            cls <-- Signal { if (dropdownOpen.value) Some("dropdown-open") else None },
            label(
              tabIndex := 0,
              idAttr := "dropdown-button",
              cls := "btn btn-ghost lg:hidden",
              icons.Hamburger(cls := "h-5 w-5"),
              onClick.foreach(e => {
                dropdownOpen.transform(!_)
              }),
            ),
            navigationMenu(
              "menu menu-compact dropdown-content mt-3 p-2 shadow bg-base-100 rounded-box w-52 dark:bg-gray-700",
            ),
          ),
        ),
        div(
          cls := "flex-1",
          a(
            icons.Reform(),
            cls := "btn btn-ghost normal-case text-xl hover:bg-slate-100 dark:hover:bg-gray-800/50",
            href := "/",
            onClick.foreach(e => {
              e.preventDefault()
              e.target.asInstanceOf[HTMLElement].blur()
              jsImplicits.routing.to(HomePage())
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
              icons.Connections(cls := "h-6 w-6"),
              span(
                cls := "badge badge-sm indicator-item dark:bg-gray-200 dark:text-gray-800",
                jsImplicits.webrtc.connections.map(_.size),
              ),
            ),
          ),
        ),
      ),
      div(cls := "p-4 mt-16 page-scroll-container overflow-x-hidden", content),
    ),
    div(
      cls := "drawer-side",
      label(forId := "connection-drawer", cls := "drawer-overlay"),
      ConnectionModal().render,
    ),
  )
}
