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

import scala.scalajs.js.annotation.*

@JSExportTopLevel("MainJSTest")
object MainJSTest extends TestSuite {

  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  val tests: Tests = Tests {}

  @JSExport
  def main(): Unit = {
    val results = TestRunner.runAndPrint(
      tests,
      "MyTestSuiteC",
    )
    TestRunner.renderResults(
      Seq(
        "MyTestSuiteC" -> results,
      ),
    )
  }
}
