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
package de.tu_darmstadt.informatik.st.reform.pages

import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.components.Modal
import de.tu_darmstadt.informatik.st.reform.components.ModalButton
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.downloadFile
import de.tu_darmstadt.informatik.st.reform.npm.*
import de.tu_darmstadt.informatik.st.reform.services.Page
import de.tu_darmstadt.informatik.st.reform.services.ToastMode
import de.tu_darmstadt.informatik.st.reform.services.ToastType
import de.tu_darmstadt.informatik.st.reform.utils.exportIndexedDBJson
import de.tu_darmstadt.informatik.st.reform.utils.importIndexedDBJson
import de.tu_darmstadt.informatik.st.reform.{*, given}
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

import scala.scalajs.js
import scala.util.Failure
import scala.util.Success

case class SettingsPage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render: VMod = {
    val deleteButtonActive = Var(false)
    val deleteDBModal = new Modal(
      "Do you really want to drop the Database?",
      div(
        cls := "flex flex-col gap-4",
        span(
          cls := "text-red-500 p-4 rounded-lg bg-red-200",
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
            CheckboxStyle.Error,
            idAttr := "export-success",
            cls := "mr-2",
            onClick.foreach(_ => deleteButtonActive.transform(!_)),
          ),
        ),
      ),
      Seq(
        new ModalButton(
          "Export Database",
          ButtonStyle.LightDefault,
          () => {
            val json = exportIndexedDBJson
            downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
          },
        ),
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
        new ModalButton("Cancel", ButtonStyle.LightDefault),
      ),
    )

    div(
      cls := "flex flex-col items-center",
      div(
        h1(cls := "text-3xl my-4 text-center", "Settings Page"),
        cls := "relative md:shadow-md md:rounded-lg py-4 px-0 md:px-4 my-4 mx-[2.5%] inline-block overflow-y-visible w-[95%] max-w-[900px]",
        div(cls := "divider"),
        div(
          cls := "md:m-4 p-4",
          h2(cls := "font-bold", "Color Scheme"),
          div(cls := "divider"),
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
            span("Nothing found"),
            false,
            false,
            cls := "max-w-[300px]",
          ),
        ),
        div(cls := "divider"),
        div(
          cls := "md:m-4 p-4",
          h2(cls := "font-bold", "Manage Database"),
          div(cls := "divider"),
          div(
            cls := "flex flex-col gap-2",
            div(
              cls := "flex flex-col gap-2",
              Button(
                ButtonStyle.Primary,
                "Export Database",
                onClick.foreach(_ => {
                  val json = exportIndexedDBJson
                  downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
                  jsImplicits.toaster.make("Database exported", ToastMode.Short, ToastType.Success)
                }),
                cls := "w-fit",
              ),
              div(
                cls := "text-slate-400 dark:text-gray-400 text-xs italic",
                "Exports the current database in a lossless JSON format. The current state of the database can be imported later.",
              ),
            ),
            div(
              cls := "flex flex-col gap-2",
              div(
                cls := "flex flex-col md:flex-row gap-2",
                label(
                  cls := "block h-[40px]",
                  span(cls := "sr-only", "Choose profile photo"),
                  input(
                    tpe := "file",
                    idAttr := "import-file",
                    cls := """block w-full text-sm text-slate-500
                            file:mr-4 file:py-2 file:px-4
                            file:rounded-lg file:border-0
                            file:text-sm file:font-semibold
                            file:bg-purple-400 file:text-purple-800
                            hover:file:bg-purple-400 h-full file:h-full dark:text-gray-400""",
                  ),
                ),
                Button(
                  ButtonStyle.Primary,
                  "Import Database",
                  onClick.foreach(_ => {
                    val fileList = document.querySelector("#import-file").asInstanceOf[HTMLInputElement].files
                    if (fileList.nonEmpty)
                      fileList(0)
                        .text()
                        .toFuture
                        .onComplete(value => {
                          if (value.isFailure) {
                            value.failed.get.printStackTrace()
                            jsImplicits.toaster.make(value.failed.get.getMessage.nn, ToastMode.Short, ToastType.Error)
                          }
                          val json = value.getOrElse("")
                          importIndexedDBJson(json).onComplete {
                            case Success(value) =>
                              jsImplicits.toaster.make("Database imported", ToastMode.Long, ToastType.Success)
                            case Failure(exception) =>
                              jsImplicits.toaster
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
              div(
                cls := "text-slate-400 dark:text-gray-400 text-xs italic",
                "Choose a JSON file that you have previously exported and it will be merged with the current db. If you want to go back to the old DB version you should version the database in the code (VITE_DATABASE_VERSION) and import it.",
              ),
            ),
            div(
              cls := "flex flex-col gap-2",
              Button(
                ButtonStyle.Error,
                "Delete Database",
                onClick.foreach(_ => {
                  val json = exportIndexedDBJson
                  downloadFile(s"reform-export-${new js.Date().toISOString()}.json", json, "data:text/json")
                  deleteDBModal.open()
                }),
                cls := "w-fit",
              ),
              div(
                cls := "text-slate-400 dark:text-gray-400 text-xs italic",
                "Deletes the local database, so make sure you have exported the data. Once a peer connects you may see deleted data again because the peer still has it.",
              ),
            ),
            hr,
            span(s"Version: ${Globals.APP_VERSION}", cls := "text-slate-400 dark:text-gray-400 text-sm italic"),
          ),
          deleteDBModal.render,
        ),
      ),
    )
  }
}
