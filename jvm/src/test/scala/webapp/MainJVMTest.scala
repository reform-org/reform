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

import loci.communicator.tcp.TCP
import loci.registry.Registry
import utest.*
import webapp.MainSharedTest.continually
import webapp.MainSharedTest.eventually
import webapp.MainSharedTest.testRepository
import webapp.entity.*
import webapp.npm.IIndexedDB
import webapp.npm.MemoryIndexedDB
import webapp.repo.Repository

import scala.scalajs.js.annotation.*

@JSExportTopLevel("MainJVMTest")
object MainJVMTest extends TestSuite {

  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  def testSyncing[T <: Entity[T]](fun: Repositories => Repository[T]) = {
    val registry0 = Registry()
    val registry1 = Registry()
    val indexedDb0: IIndexedDB = MemoryIndexedDB()
    val indexedDb1: IIndexedDB = MemoryIndexedDB()
    val repositories0 = Repositories(using registry0, indexedDb0)
    val repositories1 = Repositories(using registry1, indexedDb1)
    testRepository(fun(repositories0))
    eventually(() => fun(repositories0).all.now.length == 1)
    continually(() => fun(repositories1).all.now.length == 0)
    registry0.listen(TCP(1337))
    registry1.connect(TCP("localhost", 1337))
    eventually(() => fun(repositories1).all.now.length == 1)
    continually(() => fun(repositories1).all.now.length == 1)
    registry0.terminate()
    registry1.terminate()
  }

  val tests: Tests = Tests {
    given registry: Registry = Registry()
    given indexedDb: IIndexedDB = MemoryIndexedDB()
    given repositories: Repositories = Repositories()

    test("test syncing projects") {
      testSyncing(r => r.projects)
    }

    test("test syncing users") {
      testSyncing(r => r.users)
    }

    test("test syncing hiwis") {
      testSyncing(r => r.hiwis)
    }

    test("test syncing supervisors") {
      testSyncing(r => r.supervisors)
    }

    test("test syncing contractSchemas") {
      testSyncing(r => r.contractSchemas)
    }

    test("test syncing paymentLevels") {
      testSyncing(r => r.paymentLevels)
    }

    test("test salaryChanges projects") {
      testSyncing(r => r.salaryChanges)
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
