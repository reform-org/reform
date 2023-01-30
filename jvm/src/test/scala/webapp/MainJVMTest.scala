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
import utest.*
import webapp.entity.*
import webapp.npm.IIndexedDB
import webapp.repo.Repository
import webapp.repo.Synced
import webapp.webrtc.WebRTCService

import scala.scalajs.js.annotation.*

import webapp.npm.MemoryIndexedDB
import webapp.MainSharedTest.testRepository
import webapp.MainSharedTest.eventually
import webapp.MainSharedTest.continually

@JSExportTopLevel("MainJVMTest")
object MainJVMTest extends TestSuite {

  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  def testSyncing[T <: Entity[T]](fun: Repositories => Repository[T]) = {
    val webrtc0 = WebRTCService()
    val webrtc1 = WebRTCService()
    val indexedDb0: IIndexedDB = MemoryIndexedDB()
    val indexedDb1: IIndexedDB = MemoryIndexedDB()
    val repositories0 = Repositories(using webrtc0, indexedDb0)
    val repositories1 = Repositories(using webrtc1, indexedDb1)
    testRepository(fun(repositories0))
    eventually(() => fun(repositories0).all.now.length == 1)
    continually(() => fun(repositories1).all.now.length == 0)
    webrtc0.registry.listen(TCP(1337))
    webrtc1.registry.connect(TCP("localhost", 1337))
    eventually(() => fun(repositories1).all.now.length == 1)
    continually(() => fun(repositories1).all.now.length == 1)
    webrtc0.registry.terminate()
    webrtc1.registry.terminate()
  }

  val tests: Tests = Tests {
    given webrtc: WebRTCService = WebRTCService()
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

    test("test syncing supervisor") {
      testSyncing(r => r.supervisor)
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
