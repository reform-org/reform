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

import utest.*
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js.annotation.*

import cats.effect.SyncIO

@JSExportTopLevel("MainTests")
object MainTests extends TestSuite {
  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  def utestBeforeEach(): Unit = {
    document.body.innerHTML = ""

    // prepare body with <div id="app"></div>
    val root = document.createElement("div")
    root.id = "app"
    discard { document.body.appendChild(root) }
    ()
  }

  val tests = Tests {
    test("test") {
      utestBeforeEach()

      val message = "Hello World!"
      Outwatch.renderInto[SyncIO]("#app", h1(message)).unsafeRunSync()

      assert(document.body.innerHTML.contains(message))
    }
  }

  @JSExport
  def main(): Int = {
    val results = TestRunner.runAndPrint(
      tests,
      "MyTestSuiteA",
    )
    val (summary, successes, failures) = TestRunner.renderResults(
      Seq(
        "MyTestSuiteA" -> results,
      ),
    )
    failures
  }
}
