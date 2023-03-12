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

import webapp.components.common.*

import webapp.services.{ToastMode, ToastType, Toaster}
import webapp.given_ExecutionContext
import webapp.components.{Modal, ModalButton}
import webapp.utils.Futures.*
import webapp.utils.{exportIndexedDBJson, importIndexedDBJson}
import webapp.npm.JSUtils.downloadFile
import org.scalajs.dom.HTMLInputElement
import rescala.default.*

import scala.util.Success
import scala.util.Failure
import webapp.services.MailService
import webapp.JSImplicits

case class SettingsPage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render: VNode = {
    val deleteButtonActive = Var(false)
    val deleteDBModal = new Modal(
      "Do you really want to drop the Database?",
      div(
        cls := "flex flex-col gap-4",
        span(
          cls := "text-red-600",
          "Proceeding will drop the database and all of it's contents on your machine, so the data will be ",
          b("unrecoverably lost"),
          "!!!",
        ),
        div(
          LabeledCheckbox(
            forId := "export-success",
            cls := "text-slate-600",
            "I have verified that the export has downloaded correctly ",
          )(
            CheckboxStyle.Default,
            idAttr := "export-success",
            cls := "mr-2",
            onClick.foreach(_ => deleteButtonActive.transform(!_)),
          ),
        ),
        div(
          cls := "text-blue-300 uppercase hover:text-blue-600 cursor-pointer",
          "Export again",
          onClick.foreach(_ => {
            val json = exportIndexedDBJson
            downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
          }),
        ),
      ),
      Seq(
        new ModalButton(
          "Delete",
          ButtonStyle.Error,
          () => {
            val _ = window.indexedDB.asInstanceOf[IDBFactory].deleteDatabase("reform")
          },
          Seq(
            disabled <-- deleteButtonActive.map(!_),
          ),
        ),
        new ModalButton("Cancel"),
      ),
    )

    val multiSelectValue: Var[Seq[String]] = Var(Seq())
    val selectValue: Var[String] = Var("")

    navigationHeader(
      div(
        h1(cls := "text-3xl my-4 text-center", "Settings Page"),
        cls := "relative md:shadow-md md:rounded-lg py-4 px-0 md:px-4 my-4 mx-[2.5%] inline-block overflow-y-visible w-[95%]", // "flex flex-col gap-2 max-w-sm",
        div(
          cls := "border rounded-sm md:rounded-2xl md:m-4 border-purple-200 dark:border-gray-500 dark:text-gray-200",
          div(
            cls := "bg-purple-200 p-4 rounded-t-sm md:rounded-t-2xl dark:bg-gray-700 dark:text-gray-200",
            p("Color scheme:"),
          ),
          div(
            cls := "p-4 space-y-4 md:w-[400px]",
            Select(
              Signal(
                Seq(
                  SelectOption("dark", Signal("Dark mode")),
                  SelectOption("light", Signal("Light mode")),
                  SelectOption("default", Signal("Use browser preferences")),
                ),
              ),
              (value) => {
                window.localStorage.setItem("theme", value)
                theme.set(value)
              },
              theme,
              false,
            ),
          ),
        ),
        div(
          cls := "border rounded-sm md:rounded-2xl md:m-4 border-purple-200 dark:border-gray-500 dark:text-gray-200",
          div(
            cls := "bg-purple-200 p-4 rounded-t-sm md:rounded-t-2xl dark:bg-gray-700 dark:text-gray-200",
            p("Manage DB:"),
          ),
          div(
            cls := "flex flex-col p-4 space-y-4",
            div(
              cls := "flex flex-col md:flex-row justify-between",
              Button(
                ButtonStyle.Primary,
                "Export DB",
                onClick.foreach(_ => {
                  val json = exportIndexedDBJson
                  downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
                  toaster.make("Database exported", ToastMode.Short, ToastType.Success)
                }),
              ),
            ),
            hr,
            div(
              cls := "flex flex-col md:flex-row justify-between",
              div(
                label(
                  cls := "block",
                  span(cls := "sr-only", "Choose profile photo"),
                  input(
                    tpe := "file",
                    idAttr := "import-file",
                    cls := """block w-full text-sm text-slate-500
                            file:mr-4 file:py-2 file:px-4
                            file:rounded-full file:border-0
                            file:text-sm file:font-semibold
                            file:bg-purple-400 file:text-purple-800
                            hover:file:bg-purple-400""",
                  ),
                ),
                Button(
                  ButtonStyle.Primary,
                  cls := "mt-4",
                  "Import DB",
                  onClick.foreach(_ => {
                    val fileList = document.querySelector("#import-file").asInstanceOf[HTMLInputElement].files
                    if (fileList.nonEmpty)
                      fileList(0)
                        .text()
                        .toFuture
                        .onComplete(value => {
                          if (value.isFailure) {
                            value.failed.get.printStackTrace()
                            toaster.make(value.failed.get.getMessage.nn, ToastMode.Short, ToastType.Error)
                          }
                          val json = value.getOrElse("")
                          importIndexedDBJson(json)(using repositories).onComplete {
                            case Success(value) =>
                              toaster.make("Database imported", ToastMode.Long, ToastType.Success)
                            case Failure(exception) =>
                              toaster
                                .make(
                                  "Failed to import database! " + exception.toString,
                                  ToastMode.Long,
                                  ToastType.Error,
                                )
                          }
                        })
                  }),
                ),
              ),
            ),
            hr,
            div(
              cls := "flex flex-col md:flex-row justify-between",
              Button(
                ButtonStyle.Error,
                "Delete DB",
                onClick.foreach(_ => {
                  val json = exportIndexedDBJson
                  downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
                  deleteDBModal.open()
                }),
              ),
            ),
            deleteDBModal.render,
          ),
        ),
      ),
    )
  }
}
