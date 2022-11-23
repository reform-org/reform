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
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import webapp.given
import webapp.components.navigationHeader

case class HomePage() extends Page {
  def counter = SyncIO {
    val number = Var(0)
    div(
      cls := "grid grid-flow-col grid-rows-1 grid-cols-2",
      button(
        cls := "btn",
        "+",
        onClick(number.map(_ + 1)) --> number,
      ),
      div(
        cls := "flex justify-center items-center",
        number,
      ),
    )
  }

  def render(using services: Services): VNode =
    div(
      navigationHeader,
      div(
        cls := "p-1 grid grid-flow-col grid-rows-1 grid-cols-3 gap-1",
        a(
          cls := "btn",
          href := "/login",
          "Login",
          onClick.foreach(e => {
            e.preventDefault()
            services.routing.to(LoginPage(), true)
          }),
        ),
        a(
          cls := "btn",
          href := "/project/Wir sind schon die besten lol",
          "Beispielprojekt",
          onClick.foreach(e => {
            e.preventDefault()
            services.routing.to(ProjectPage("Wir sind schon die besten lol"), true)
          }),
        ),
        counter,
      ),
    )
}
