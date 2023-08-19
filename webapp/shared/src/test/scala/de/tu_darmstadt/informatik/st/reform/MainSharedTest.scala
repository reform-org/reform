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
package de.tu_darmstadt.informatik.st.reform

import loci.registry.Registry
import utest.*
import de.tu_darmstadt.informatik.st.reform.entity.*
import de.tu_darmstadt.informatik.st.reform.npm.IIndexedDB
import de.tu_darmstadt.informatik.st.reform.npm.MemoryIndexedDB
import de.tu_darmstadt.informatik.st.reform.repo.Repository
import de.tu_darmstadt.informatik.st.reform.repo.Synced
import de.tu_darmstadt.informatik.st.reform.utils.Seqnal.*
import scala.concurrent.Future

import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import scala.concurrent.Promise
import rescala.default.*
import rescala.core.Disconnectable
import scala.annotation.nowarn

object MainSharedTest extends TestSuite {

  @specialized def discard[A](evaluateForSideEffectOnly: A): Unit = {
    val _ = evaluateForSideEffectOnly
    () // Return unit to prevent warning due to discarding value
  }

  def testE[T <: Entity[T]](value: Synced[T]): T = {
    val now = value.signal.now
    now.identifier.getAll
    assert(!now.withExists(false).exists)
    now.default
  }

  def testRepository[T <: Entity[T]](repository: Repository[T]) = {
    for _ <- repository.all.waitUntil(_.isEmpty)
    _ <- repository
      .create(repository.defaultValue)
      .map(value => testE(value))
    _ <- repository.all.waitUntil(_.length == 1)
    yield ()
  }

  val tests: Tests = Tests {
    given registry: Registry = Registry()
    given indexedDb: IIndexedDB = MemoryIndexedDB()
    given repositories: Repositories = Repositories()

    test("test projects repository") {
      testRepository(repositories.projects)
    }

    test("test users repository") {
      testRepository(repositories.users)
    }

    test("test hiwis repository") {
      testRepository(repositories.hiwis)
    }

    test("test supervisors repository") {
      testRepository(repositories.supervisors)
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
