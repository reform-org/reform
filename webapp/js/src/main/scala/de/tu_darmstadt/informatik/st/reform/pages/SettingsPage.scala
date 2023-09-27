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
import scala.util.Failure

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

    val multiSelectValue: Var[Seq[String]] = Var(Seq())
    val selectValue: Var[String] = Var("")

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
            value => {
              window.localStorage.setItem("theme", value)
              theme.set(value)
            },
            theme,
            false,
            span("Nothing found"),
            false,
            false,
            cls := "max-w-[300px] rounded-md",
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
                cls := "md:w-fit",
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
                FileInput(idAttr := "import-file"),
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
                cls := "md:w-fit",
              ),
              div(
                cls := "text-slate-400 dark:text-gray-400 text-xs italic",
                "Deletes the local database, so make sure you have exported the data. Once a peer connects you may see deleted data again because the peer still has it.",
              ),
            ),
          ),
          deleteDBModal.render,
        ),
        div(cls := "divider"),
        div(
          cls := "md:m-4 p-4",
          h2(cls := "font-bold", "Inspect PDF Fields"),
          div(cls := "divider"),
          div(
            cls := "flex flex-col md:flex-row gap-2",
            FileInput(idAttr := "inspect-pdf", accept := ".pdf"),
            Button(
              ButtonStyle.Primary,
              "Inspect PDF",
              onClick.foreach(_ => {
                val fileList = document.querySelector("#inspect-pdf").asInstanceOf[HTMLInputElement].files
                if (fileList.nonEmpty)
                  fileList(0)
                    .arrayBuffer()
                    .toFuture
                    .flatMap(buffer => getPDFFields(buffer))
                    .onComplete(fields => {
                      fields match {
                        case Failure(exception) =>
                          jsImplicits.toaster
                            .make(s"Exception: ${exception.getMessage()}", ToastMode.Infinit, ToastType.Error)
                        case Success(fields) => pdfFields.set(fields)
                      }
                    })
              }),
            ),
          ),
          Signal {
            val fields = pdfFields.value
            if (fields.size > 0) {
              Some(
                div(
                  cls := "my-2 flex flex-col text-sm rounded-lg bg-slate-100 dark:bg-gray-700/50 p-2 relative",
                  div(
                    icons.Close(cls := "text-red-600 w-4 h-4"),
                    cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer absolute right-2 top-2",
                    data.tip := "Close",
                    onClick.foreach(_ => {
                      resetFields
                    }),
                  ),
                  div(
                    cls := "text-slate-400 dark:text-gray-400 text-xs italic mb-2 mt-8 md:mt-2",
                    "The fields are displayed in the order that they appear in the form and are presented as <type>:<name> [<value>]. So if you are looking for a specific textfield just fill it with your text and then search in the list below for this text. If you are looking for the name of a checkbox, check it and look for the text \"checked\".",
                  ),
                  fields.map(field => div(field)),
                ),
              )
            } else None
          },
          div(
            cls := "text-slate-400 dark:text-gray-400 text-xs italic my-2",
            "The fields are displayed in the order that they appear in the form and are presented as <type>:<name> [<value>]. So if you are looking for a specific textfield just fill it with your text and then search in the list below for this text. If you are looking for the name of a checkbox, check it and look for the text \"checked\".",
            "If the Names are not clear enough you might want to use a PDF Editor like Adobe Acrobat Pro, here you can click on Prepare Form which opens a list with all the labels and renders the label name inside the fields in the PDF.",
          ),
        ),
        hr,
        span(s"Version: ${Globals.APP_VERSION}", cls := "text-slate-400 dark:text-gray-400 text-sm italic"),
      ),
    )
  }
}
