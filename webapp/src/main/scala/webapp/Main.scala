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
package webapp

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO
import rescala.default.{Event, Signal, Var}
import colibri.{Cancelable, Observer, Source, Subject}
import scala.scalajs.js
import js.annotation._

// object JavaScriptHot {
//   @js.native
//   @JSGlobal("accept")
//   def accept(): Unit = js.native
// }

// object JavaScriptMeta {
//   @js.native
//   @JSGlobal("hot")
//   val hot: JavaScriptHot
// }

// object JavaScriptImport {
//   @js.native
//   @JSGlobal("meta")
//   val meta: JavaScriptMeta
// }

// object DOMGlobals {
//   @js.native
//   @JSGlobal("import")
//   val javascriptImport: JavaScriptImport = js.native

//   def magic(): Unit = {
//     if (javascriptImport.meta.hot) {
//       DOMGlobals.javascriptImport.meta.hot.accept()
//     }
//   }
// }

// https://simerplaha.github.io/html-to-scala-converter/
object Main {
  def main(): Unit =
    Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

  def app = div(
    h1(cls := "font-bold underline", "Hello world!"),
    counter,
    inputField,
    cls := "h-56 grid grid-cols-3 gap-4 content-center",
  )

  def counter = SyncIO {
    // https://outwatch.github.io/docs/readme.html#example-counter
    val number = Var(0)
    div(
      button("+", onClick(number.map(_ + 1)) --> number, marginRight := "10px"),
      number,
    )
  }

  def inputField = SyncIO {
    // https://outwatch.github.io/docs/readme.html#example-input-field
    val text = Var("")
    div(
      input(
        tpe := "text",
        value <-- text,
        onInput.value --> text,
      ),
      button("clear", onClick.as("") --> text),
      div("text: ", text),
      div("length: ", text.map(_.length)),
    )
  }
}

val _ = Main.main()
