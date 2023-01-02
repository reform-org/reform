/*
Copyright 2022 https://github.com/phisn/ratable, The reform-org/reform contributors

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
package webapp.services

import colibri.*
import colibri.router.*
import colibri.router.Router
import org.scalajs.dom.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import webapp.*
import webapp.pages.*

trait Page {
  def render(using services: Services): VNode
}

class RoutingState(
    // if canReturn is true then the page will show in mobile mode
    // an go back arrow in the top left corner
    val canReturn: Boolean,
) extends js.Object

class RoutingService() {
  private val page = Var[Page](Routes.fromPath(Path(window.location.pathname)))

  def render(using services: Services): Signal[VNode] =
    page.map(_.render)

  def to(newPage: Page, preventReturn: Boolean = false) = {
    window.history.pushState(RoutingState(!preventReturn), "", linkPath(newPage))
    page.set(newPage)
  }

  def link(newPage: Page) =
    URL(linkPath(newPage), window.location.href).toString

  def linkPath(newPage: Page) =
    Routes.toPath(newPage).pathString

  def back() =
    window.history.back()

  def state =
    window.history.state.asInstanceOf[RoutingState]

  // Ensure initial path is correctly set
  // Example: for path "/counter" and pattern "counter/{number=0}" the
  //          url should be "/counter/0" and not "/counter"
  window.history.replaceState(RoutingState(false), "", linkPath(page.now))

  // Change path when url changes by user action
  window.onpopstate = _ => page.set(Routes.fromPath(Path(window.location.pathname)))
}
