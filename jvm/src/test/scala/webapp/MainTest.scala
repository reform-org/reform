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
import webapp.entity.*
import webapp.{*, given}
import loci.communicator.tcp.TCP
import loci.registry.Registry
import webapp.repo.Synced

@JSExportTopLevel("MainTest")
object MainTest extends TestSuite {
  
  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  def testE[T <: Entity[T]](value: Synced[T]) = {
    value.signal.now.exists
    value.signal.now.identifier
    value.signal.now.withExists(false).exists
    value.signal.now.default
  }

  def testRepository[T <: Entity[T]](repository: Repository[T]) = {
    assert(repository.all.now.length == 0)
    repository
      .create()
      .map(value => testE(value))
    eventually(repository.all.now.length == 1)
    continually(repository.all.now.length == 1)
  }

  val tests: Tests = Tests {
    given registry: Registry = Registry()
    given repositories: Repositories = Repositories()

    test("test projects repository") {
      testRepository(repositories.projects)
    }

    test("syncing") {
      val registry0 = Registry()
      val registry1 = Registry()
      val repositories0 = Repositories(using registry0)
      val repositories1 = Repositories(using registry1)
      testRepository(repositories0.projects)
      eventually(repositories0.projects.all.now.length == 1)
      continually(repositories1.projects.all.now.length == 0)
      registry0.listen(TCP(1337))
      registry1.connect(TCP("localhost", 1337))
      eventually(repositories1.projects.all.now.length == 1)
      continually(repositories1.projects.all.now.length == 1)
    }

    test("test users repository") {
      testRepository(repositories.users)
    }

    test("test hiwis repository") {
      testRepository(repositories.hiwis)
    }

    test("test supervisor repository") {
      testRepository(repositories.supervisor)
    }

    test("test contractSchemas repository") {
      testRepository(repositories.contractSchemas)
    }

    test("test paymentLevels repository") {
      testRepository(repositories.paymentLevels)
    }

    test("test salaryChanges repository") {
      testRepository(repositories.salaryChanges)
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
