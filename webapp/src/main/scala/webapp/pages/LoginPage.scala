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

import org.scalajs.dom
import outwatch._
import outwatch.dsl._
import rescala.default._
import webapp.services._
import webapp._

case class LoginPage() extends Page:
  def render(using services: Services): VNode =
    div(
      h1(cls := "font-bold underline", "Login page"),
      cls := "h-56 grid grid-cols-3 gap-4 content-center",
      a(
        href := "/",
        "Home",
        onClick.foreach(e => {
          e.preventDefault()
          services.routing.to(HomePage(), true)
        }),
      ),
    )
