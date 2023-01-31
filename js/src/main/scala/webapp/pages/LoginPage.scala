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
package webapp.pages

import outwatch.*
import outwatch.dsl.*
import webapp.*
import webapp.components.navigationHeader
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.services.DiscoveryService

case class LoginPage() extends Page {
  def render(using routing: RoutingService, repositories: Repositories, webrtc: WebRTCService, discovery: DiscoveryService): VNode =
    navigationHeader(
      div(
        div(
          cls := "p-1",
          h1(cls := "text-4xl text-center", "Login page"),
        ),
      )
    )
}
