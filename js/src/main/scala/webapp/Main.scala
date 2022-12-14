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

import outwatch.*
import outwatch.dsl.*
import cats.effect.SyncIO
import rescala.default.{Event, Signal, Var}
import colibri.{Cancelable, Observer, Source, Subject}
import scala.scalajs.js
import js.annotation.*
import webapp.services.*

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
  def main(): Unit = {
    js.`import`("../../../../index.css")
    implicit val services = ServicesDefault
    Outwatch.renderInto[SyncIO]("#app", app()).unsafeRunSync()
  }

  def app(using services: Services) = body(
    services.routing.render,
  )
}

val _ = Main.main()
