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

case class SettingsPage()(using indexeddb: IIndexedDB) extends Page {

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
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
        p("Settings Page"),
        cls := "flex flex-col gap-2 max-w-sm",
        p("Color scheme:"),
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
        /*
        Button(
          ButtonStyle.LightDefault,
          // cls := "btn btn-active p-2 h-fit min-h-10 border-0",
          "Make me a boring normal toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", ToastMode.Short, ToastType.Default)
          }),
        ),
        Button(
          ButtonStyle.Success,
          "Make me a successful toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", ToastMode.Short, ToastType.Success)
          }),
        ),
        Button(
          ButtonStyle.Warning,
          "Make me a warning toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", ToastMode.Short, ToastType.Warning)
          }),
        ),
        Button(
          ButtonStyle.Error,
          "Make me an error toast 🍞",
          onClick.foreach(_ => {
            toaster.make(
              "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam 🍞",
              ToastMode.Short,
              ToastType.Error,
            )
          }),
        ),
        Button(
          ButtonStyle.Error,
          "Make me a persistent error toast 🍞",
          onClick.foreach(_ => {
            toaster.make("Here is your toast 🍞", ToastMode.Infinit, ToastType.Error)
          }),
        ),
        Button(
          ButtonStyle.Primary,
          "Export DB",
          onClick.foreach(_ => {
            val json = exportIndexedDBJson
            downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
            toaster.make("Database exported", ToastMode.Short, ToastType.Success)
          }),
        ),
        FileInput(tpe := "file", idAttr := "import-file"),
        Button(
          ButtonStyle.Primary,
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
                    case Success(value) => toaster.make("Database imported", ToastMode.Long, ToastType.Success)
                    case Failure(exception) =>
                      toaster
                        .make("Failed to import database! " + exception.toString, ToastMode.Long, ToastType.Error)
                  }
                })
          }),
        ),
        Button(
          ButtonStyle.Error,
          "Delete DB",
          onClick.foreach(_ => {
            val json = exportIndexedDBJson
            downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
            deleteDBModal.open()

          }),
        ),
        deleteDBModal.render,
        MultiSelect(
          Signal(List(MultiSelectOption("test", Signal("test")), MultiSelectOption("test2", Signal("test2")))),
          (value) => multiSelectValue.set(value),
          multiSelectValue,
        ),
        Select(
          Signal(List(SelectOption("test3", Signal("test")), SelectOption("test4", Signal("test2")))),
          (value) => selectValue.set(value),
          selectValue,
        ),
         */
      ),
    )
  }
}
