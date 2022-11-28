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
import webapp.components.navigationHeader

case class ProjectPage(id: String) extends Page {
  def render(using services: Services): VNode =
    div(
      navigationHeader,
      table(
        cls := "table-auto",
        thead(
          tr(
            th("Song"),
            th("Artist"),
            th("Year"),
          ),
        ),
        tbody(
          tr(
            td(
              cls := "text-sm text-gray-900 font-light px-6 py-4 whitespace-nowrap  nowrap",
              "The Sliding Mr. Bones (Next Stop, Pottersville)",
            ),
            td("Malcolm Lockyer"),
            td("1961"),
          ),
          tr(
            td("Witchy Woman"),
            td("The Eagles"),
            td("1972"),
          ),
          tr(
            td("Shining Star"),
            td("Earth, Wind, and Fire"),
            td("1975"),
          ),
        ),
      ),
    )
}
