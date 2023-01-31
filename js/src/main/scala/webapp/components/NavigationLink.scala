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
import webapp.*
import org.scalajs.dom.HTMLElement
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService

def navigationLink(using routing: RoutingService)(page: Page, label: String): VNode = {
  a(
    cls := "focus:bg-slate-200",
    label,
    onClick.foreach(e => {
      e.preventDefault()
      e.target.asInstanceOf[HTMLElement].blur()
      routing.to(page, true)
    }),
    href := routing.linkPath(page),
  )
}
