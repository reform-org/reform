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

import concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.*
import webapp.repo.Repository
import webapp.Repositories.*
import webapp.entity.*
import webapp.{*, given}
import webapp.webrtc.WebRTCService
import loci.communicator.tcp.TCP

@JSExportTopLevel("MainTest")
object MainTest extends TestSuite {
  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  def testRepository[T <: Entity[T]](repository: Repository[T]) = {
      assert(repository.all.now.length == 0)
      repository.create().map(value => {
        value.signal.now.exists
        value.signal.now.identifier
        value.signal.now.withExists(false).exists
        value.signal.now.default
      })
      eventually(repository.all.now.length == 1)
      continually(repository.all.now.length == 1)
  }

  val tests: Tests = Tests {
    test("syncing") {
      WebRTCService.registry.listen(TCP(1337))
      WebRTCService.registry.connect(TCP("localhost", 1337))
    }

    test("test projects repository") {
      testRepository(projects)
    }

    test("test users repository") {
      testRepository(users)
    }

    test("test hiwis repository") {
      testRepository(hiwis)
    }

    test("test supervisor repository") {
      testRepository(supervisor)
    }

    test("test contractSchemas repository") {
      testRepository(contractSchemas)
    }

    test("test paymentLevels repository") {
      testRepository(paymentLevels)
    }

    test("test salaryChanges repository") {
      testRepository(salaryChanges)
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
