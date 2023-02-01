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
import webapp.entity.*
import webapp.npm.IIndexedDB
import webapp.npm.MemoryIndexedDB
import webapp.repo.Repository
import webapp.repo.Synced
import webapp.webrtc.WebRTCService

import scala.scalajs.js.annotation.*

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import loci.registry.Registry

@JSExportTopLevel("MainJSTest")
object MainJSTest extends TestSuite {

  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  val tests: Tests = Tests {
    given registry: Registry = Registry()
    given webrtc: WebRTCService = WebRTCService()
    given indexedDb: IIndexedDB = MemoryIndexedDB()
    given repositories: Repositories = Repositories()

  }

  @JSExport
  def main(): Unit = {
    val results = TestRunner.runAndPrint(
      tests,
      "MyTestSuiteC",
    )
    val (summary, successes, failures) = TestRunner.renderResults(
      Seq(
        "MyTestSuiteC" -> results,
      ),
    )
    failures
  }
}
