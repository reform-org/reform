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

def navigationMenu(using services: Services)(classes: String) = {
  ul(
    tabIndex := 0,
    cls := classes,
    li(
      a(
        "Projects",
        href := "/projects",
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          services.routing.to(ProjectsPage(), true)
        }),
      ),
    ),
    li(
      a(
        "Users",
        href := "/users",
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          services.routing.to(UsersPage(), true)
        }),
      ),
    ),
    li(
      a(
        "Paymentlevels",
        href := "/paymentlevels",
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          services.routing.to(PaymentLevelsPage(), true)
        }),
      ),
    ),
    li(
      a(
        "SalaryChanges",
        href := "/salarychanges",
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          services.routing.to(SalaryChangesPage(), true)
        }),
      ),
    ),
    li(
      a(
        "Hiwis",
        href := "/hiwis",
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          services.routing.to(HiwisPage(), true)
        }),
      ),
    ),
    li(
      a(
        "Supervisors",
        href := "/supervisors",
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          services.routing.to(SupervisorsPage(), true)
        }),
      ),
    ),
    li(
      a(
        "ContractSchemas",
        href := "/contractSchemas",
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          services.routing.to(SupervisorsPage(), true)
        }),
      ),
    ),
  )
}

def navigationHeader(using services: Services) = {
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
          services.routing.to(HomePage(), true)
        }),
      ),
    ),
    div(
      cls := "navbar-center hidden lg:flex",
      navigationMenu("menu menu-horizontal p-0"),
    ),
    ConnectionModal(using services).render,
  )
}
