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
import webapp.*
import webapp.components.navigationHeader
import webapp.npm.*
import webapp.services.DiscoveryService
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService

import webapp.services.{ToastType, Toaster}
import concurrent.ExecutionContext.Implicits.global
import webapp.components.{Modal, ModalButton}

case class HomePage() extends Page {

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    val modal = new Modal(
      "Title",
      "Creative Text",
      Seq(
        new ModalButton(
          "Yay!",
          "bg-purple-600",
          () => {
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
              .onComplete(value => {
                if (value.isFailure) {
                  // TODO FIXME show Toast
                  value.failed.get.printStackTrace()
                  toaster.make(value.failed.get.getMessage().nn, true)
                  // window.alert(value.failed.get.getMessage().nn)
                }
              })
          },
        ),
        new ModalButton("Nay!"),
      ),
    )

    navigationHeader(
      div(
        cls := "flex flex-col gap-2 max-w-sm",
        p("Homepage"),
        button(
          cls := "btn btn-active p-2 h-fit min-h-10 border-0",
          idAttr := "loadPDF",
          "Fill PDF",
          onClick.foreach(_ => {
            modal.open()
          }),
        ),
        button(
          cls := "btn btn-active p-2 h-fit min-h-10 border-0",
          idAttr := "makeToast",
          "Make me a boring normal toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", true, ToastType.Default)
          }),
        ),
        button(
          cls := "btn btn-active btn-success p-2 h-fit min-h-10 border-0",
          idAttr := "makeToast",
          "Make me a successful toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", true, ToastType.Success)
          }),
        ),
        button(
          cls := "btn btn-active btn-warning p-2 h-fit min-h-10 border-0",
          idAttr := "makeToast",
          "Make me a warning toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", true, ToastType.Warning)
          }),
        ),
        button(
          cls := "btn btn-active btn-error p-2 h-fit min-h-10 border-0",
          idAttr := "makeToast",
          "Make me an error toast 🍞",
          onClick.foreach(_ => {
            toaster.make(
              "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam 🍞",
              true,
              ToastType.Error,
            )
          }),
        ),
        button(
          cls := "btn btn-active btn-error p-2 h-fit min-h-10 border-0",
          idAttr := "makeToast",
          "Make me a persistent error toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", false, ToastType.Error)
          }),
        ),
        modal.render(),
      ),
    )
  }
}
