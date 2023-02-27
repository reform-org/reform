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
import org.scalajs.dom
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*

import webapp.webrtc.WebRTCService
import webapp.npm.JSUtils.cleanPopper

import scala.scalajs.js
import webapp.npm.IIndexedDB

trait Page {
  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode
}

class RoutingService(using repositories: Repositories, toaster: Toaster, indexedb: IIndexedDB) {
  given RoutingService = this;

  private lazy val page = Var[Page](Routes.fromPath(Path(window.location.pathname)))

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): Signal[VNode] =
    page.map(_.render)

  def to(newPage: Page, preventReturn: Boolean = false, newTab: Boolean = false) = {
    if (newTab) {
      window.open(linkPath(newPage), "_blank").focus();
    } else {
      window.history.pushState(null, "", linkPath(newPage))
      cleanPopper()
      page.set(newPage)
    }

    document.activeElement.asInstanceOf[HTMLElement].blur()
  }

  def link(newPage: Page) =
    URL(linkPath(newPage), window.location.href).toString

  def linkPath(newPage: Page) =
    Routes.toPath(newPage).pathString

  def back() =
    window.history.back()

  window.history.replaceState(null, "", linkPath(page.now))

  window.onpopstate = _ => page.set(Routes.fromPath(Path(window.location.pathname)))
}
