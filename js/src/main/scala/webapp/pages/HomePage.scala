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
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import webapp.given
import webapp.components.navigationHeader
import webapp.services.Page
import webapp.webrtc.CounterService

import concurrent.ExecutionContext.Implicits.global

case class HomePage() extends Page {

  def counter(using services: Services) = SyncIO {
    div(
      cls := "grid grid-flow-col grid-rows-1 grid-cols-2",
      button(
        cls := "btn",
        "+",
        onClick.foreach(_ => CounterService.counter.map(_.incrementValueEvent.fire(1))),
      ),
      div(
        cls := "flex justify-center items-center",
        CounterService.counter.map(_.signal.map(_.value)),
      ),
    )
  }

  def render(using services: Services): VNode =
    div(
      navigationHeader,
      div(
        cls := "p-1 grid grid-flow-col grid-rows-1 grid-cols-3 gap-1",
        counter,
      ),
    )
}
