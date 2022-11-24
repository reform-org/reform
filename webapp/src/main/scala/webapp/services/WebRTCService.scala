/*
Copyright 2022 https://github.com/phisn/ratable, The reform-org/reform contributors

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
package webapp.services

import colibri.*
import colibri.router.*
import colibri.router.Router
import loci.registry.Registry
import org.scalajs.dom.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import webapp.*
import webapp.pages.*
import kofre.decompose.containers.DeltaBufferRDT
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import java.util.concurrent.ThreadLocalRandom
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.decompose.interfaces.MVRegisterInterface.MVRegisterSyntax
import kofre.datatypes.TimedVal

import kofre.datatypes.TimedVal
import kofre.decompose.containers.DeltaBufferRDT
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegisterSyntax
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import kofre.decompose.interfaces.MVRegisterInterface.MVRegisterSyntax
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import loci.registry.Binding
import org.scalajs.dom.UIEvent
import org.scalajs.dom.html.{Input, LI}
import rescala.default.*
import rescala.extra.Tags.*
import scala.Function.const
import scala.collection.mutable
import scala.scalajs.js.timers.setTimeout

class WebRTCService() {
  val registry = new Registry

  // every client has an id
  val replicaID: String = ThreadLocalRandom.current().nextLong().toHexString

  def test() = {
    // a last writer wins register. This means the last value written is the actual value.
    val lastWriterWinsInit = DeltaBufferRDT(replicaID, LWWRegisterInterface.empty[Int])
    // initialize the last writer wins with 0 and our replica id
    val lastWriterWins: DeltaBufferRDT[LWWRegister[Int]] =
      MVRegisterSyntax(lastWriterWinsInit).write(TimedVal(0, lastWriterWinsInit.replicaID, 0, 0));

    // event that fires when the user wants to change the value
    val testChangeEvent = rescala.default.Evt[Int]();

    // event that fires when changes from other peers are received
    val deltaEvent = Evt[DottedName[LWWRegister[Int]]]()

    // TODO FIXME Storing.storedAs persists this value

    // TaskData.scala
    // look at foldAll documentation+example
    val counterSignal: Signal[DeltaBufferRDT[LWWRegister[Int]]] = Events.foldAll(lastWriterWins)(current => {
      Seq(
        // if the user changes the value, update the register with the new value
        testChangeEvent.act2({ v =>
          {
            current.resetDeltaBuffer().map(current => current + v)
          }
        }),
        // if we receive a delta from a peer, apply it
        deltaEvent.act2({ delta => current.resetDeltaBuffer().applyDelta(delta) }),
      )
    })

    // magic to convert our counterSignal to the value inside
    val taskData = counterSignal.map(x => LWWRegisterInterface.LWWRegisterSyntax(x).read.getOrElse(1337))

    val t = new java.util.Timer()
    val task = new java.util.TimerTask {
      def run() = {
        val test: Int = taskData.now;
        println(test)
        testChangeEvent.fire(1)
      }
    }
    t.schedule(task, 1000L, 1000L)
  }
}
