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
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import webapp.given
import webapp.components.navigationHeader
import webapp.services.Page

import concurrent.ExecutionContext.Implicits.global
import webapp.npm.*
import webapp.services.RoutingService
import loci.registry.Registry

case class HomePage() extends Page {

  def render(using routing: RoutingService, repositories: Repositories, registry: Registry): VNode =
    div(
      navigationHeader,
      p("Homepage"),
      button(
        cls := "btn",
        idAttr := "loadPDF",
        "Fill PDF",
        onClick.foreach(_ => {
          document.getElementById("loadPDF").classList.add("loading")
          PDF
            .fill(
              "contract_unlocked.pdf",
              "arbeitsvertrag2.pdf",
              Seq(
                PDFTextField("Vorname Nachname (Studentische Hilfskraft)", "Lukas Schreiber"),
                PDFTextField("Geburtsdatum (Studentische Hilfskraft)", "25.01.1999"),
                PDFTextField("Vertragsbeginn", "25.01.2023"),
                PDFTextField("Vertragsende", "25.01.2024"),
                PDFTextField("Arbeitszeit Kästchen 1", "20 h"),
                PDFCheckboxField("Arbeitszeit Kontrollkästchen 1", true),
                PDFCheckboxField("Vergütung Kontrollkästchen 1", false),
                PDFCheckboxField("Vergütung Kontrollkästchen 2", true),
              ),
            )
            .andThen(s => {
              console.log(s)
              document.getElementById("loadPDF").classList.remove("loading")
            })
        }),
      ),
    )
}
