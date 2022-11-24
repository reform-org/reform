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

  val replicaId: String = ThreadLocalRandom.current().nextLong().toHexString

  def test() = {
    val lwwInit = DeltaBufferRDT(replicaId, LWWRegisterInterface.empty[Int])

    val lww: DeltaBufferRDT[LWWRegister[Int]] = MVRegisterSyntax(lwwInit).write(TimedVal(0, lwwInit.replicaID, 0, 0));

    val testValue = rescala.default.Event[Int](None);

    val deltaEvt = Evt[DottedName[LWWRegister[Int]]]()

    // TODO FIXME Storing.storedAs persists this value

    // createTaskRef
    val counterSignalPeerA: Signal[DeltaBufferRDT[LWWRegister[Int]]] = Events.foldAll(lww)(current => {
      Seq(
        testValue.act2({ v => current.resetDeltaBuffer().map(_ => v) }),
        deltaEvt.act2({ delta => current.resetDeltaBuffer().applyDelta(delta) }),
      )
    })

    // testValue.set(1)
  }
}
