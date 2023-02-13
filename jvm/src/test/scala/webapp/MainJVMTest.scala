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
import webapp.MainSharedTest.testRepository
import webapp.MainSharedTest.waitUntilTrue
import webapp.entity.*
import webapp.npm.IIndexedDB
import webapp.npm.MemoryIndexedDB
import webapp.repo.Repository
import webapp.given_ExecutionContext

import scala.scalajs.js.annotation.*

@JSExportTopLevel("MainJVMTest")
object MainJVMTest extends TestSuite {

  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  def testSyncing[T <: Entity[T]](fun: Repositories => Repository[T], port: Int) = {
    val registry0 = Registry()
    val registry1 = Registry()
    val indexedDb0: IIndexedDB = MemoryIndexedDB()
    val indexedDb1: IIndexedDB = MemoryIndexedDB()
    val repositories0 = Repositories()(using registry0, indexedDb0)
    val repositories1 = Repositories()(using registry1, indexedDb1)
    for _ <- testRepository(fun(repositories0))
    _ <- waitUntilTrue(fun(repositories0).all.map(_.length == 1))
    _ <- waitUntilTrue(fun(repositories1).all.map(_.length == 0))
    _ = registry0.listen(TCP(port))
    _ <- registry1.connect(TCP("localhost", port))
    _ <- waitUntilTrue(fun(repositories1).all.map(_.length == 1))
    _ <- waitUntilTrue(fun(repositories1).all.map(_.length == 1))
    _ = registry0.terminate()
    _ = registry1.terminate()
    yield ()
  }

  val tests: Tests = Tests {

    test("test syncing projects") {
      testSyncing(r => r.projects, 1337)
    }

    test("test syncing users") {
      testSyncing(r => r.users, 1338)
    }

    test("test syncing hiwis") {
      testSyncing(r => r.hiwis, 1339)
    }

    test("test syncing supervisors") {
      testSyncing(r => r.supervisors, 1340)
    }

    test("test syncing contractSchemas") {
      testSyncing(r => r.contractSchemas, 1341)
    }

    test("test syncing paymentLevels") {
      testSyncing(r => r.paymentLevels, 1342)
    }

    test("test syncing salaryChanges") {
      testSyncing(r => r.salaryChanges, 1343)
    }
  }

  @JSExport
  def main(): Int = {
    val results = TestRunner.runAndPrint(
      tests,
      "MyTestSuiteA",
    )
    val (_, _, failures) = TestRunner.renderResults(
      Seq(
        "MyTestSuiteA" -> results,
      ),
    )
    failures
  }
}
