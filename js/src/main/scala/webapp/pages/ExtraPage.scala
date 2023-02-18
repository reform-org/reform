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

import org.scalajs.dom.*
import scala.scalajs.js
import outwatch.*
import outwatch.dsl.*
import webapp.{*, given}
import webapp.components.navigationHeader
import webapp.npm.*
import webapp.services.DiscoveryService
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import rescala.default.*

import webapp.components.common.*
import webapp.services.Toaster

case class ExtraPage() extends Page {

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    val multiSelectValue: Var[Seq[String]] = Var(Seq())

    navigationHeader(
      table(
        cls := "grid border-collapse min-w-full",
        styleAttr := "grid-template-columns: minmax(150px, 1fr) minmax(150px, 1.67fr) minmax(150px, 1.67fr) minmax(150px, 1.67fr) minmax(150px, 1.67fr) minmax(150px, 3.33fr) minmax(150px, 3.33fr) minmax(150px, 1.67fr);",
        thead(
          cls := "contents",
          tr(
            cls := "contents",
            th(
              VMod.attr("data-type") := "numeric",
              "ID",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
            th(
              VMod.attr("data-type") := "text-short",
              "First name",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
            th(
              VMod.attr("data-type") := "text-short",
              "Last name",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
            th(
              VMod.attr("data-type") := "text-short",
              "Email",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
            th(
              VMod.attr("data-type") := "text-long",
              "Street",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
            th(
              VMod.attr("data-type") := "text-short",
              "Country",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
            th(
              VMod.attr("data-type") := "text-long",
              "University",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
            th(
              VMod.attr("data-type") := "text-short",
              "IBAN",
              span(cls := "resize-handle"),
              cls := "sticky top-0 bg-purple-400",
            ),
          ),
        ),
        tbody(
          cls := "contents",
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000001"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Lani"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Ovendale"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "lovendale0@w3.org"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "7850 Old Shore Drive"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "United Kingdom"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "University of Plymouth"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "BG34 MPVP 8782 88EX H1CJ SC"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000002"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Israel"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Tassell"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "itassell1@ow.ly"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "245 Merchant Circle"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Macedonia"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "South East European University"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "FR11 4824 2942 41H9 XBHC 46P2 O86"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000003"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Eveleen"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Mercer"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "emercer2@ow.ly"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "70700 Kipling Pass"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Portugal"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Instituto Superior Novas Profissões - INP"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "GR96 7559 456P GUAN WTAJ 3VPB S0P"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000004"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Conn"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Whitley"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "cwhitley3@wsj.com"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "03 Service Terrace"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Albania"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Epoka University"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "LI59 1813 2T7T VKTO 6RKE X"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000005"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Cherye"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Smitheram"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "csmitheram4@rambler.ru"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "9 Eliot Parkway"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Indonesia"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Universitas Mahasaraswati Denpasar"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "BR27 4570 4226 4255 5239 0197 316T J"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000006"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Bunnie"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Sked"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "bsked5@51.la"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "03418 Ludington Plaza"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Nigeria"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Federal University of Technology, Minna"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "ES45 6721 1332 3288 6455 1242"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000007"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Helaine"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Crother"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "hcrother6@opera.com"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "7932 Sloan Park"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Philippines"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Saint Ferdinand College"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "GB50 HFAD 8121 3729 9841 31"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000008"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Tana"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Ajean"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "tajean7@sfgate.com"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "2 Schurz Junction"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "China"),
            td(
              cls := "text-ellipsis whitespace-nowrap overflow-hidden",
              "Xi'an University of Electronic Science and Technology",
            ),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "KZ85 7422 XDPB P2VQ H8SR"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000009"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Sollie"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Greenrde"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "sgreenrde8@wikispaces.com"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "055 Mockingbird Way"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Russia"),
            td(
              cls := "text-ellipsis whitespace-nowrap overflow-hidden",
              "St. Petersburg State Mining Institute (Technical University)",
            ),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "CH61 6423 9T4W WP0I 8MUK C"),
          ),
          tr(
            cls := "contents",
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "000010"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Vernon"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Millington"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "vmillington9@marketwatch.com"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "74 David Pass"),
            td(
              cls := "",
              MultiSelect(
                Signal(List(SelectOption("test", Signal("test")), SelectOption("test2", Signal("test2")))),
                (value) => multiSelectValue.set(value),
                multiSelectValue,
              ),
            ),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "Instituto Politécnico de Setúbal"),
            td(cls := "text-ellipsis whitespace-nowrap overflow-hidden", "ES71 2390 0665 1601 8072 4924"),
          ),
        ),
      ),
    )
  }
}
