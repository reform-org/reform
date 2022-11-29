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
package webapp.services

import kofre.decompose.containers.DeltaBufferRDT
import kofre.datatypes.PosNegCounter
import scala.concurrent.Future
import scala.scalajs.js.JSON
import kofre.syntax.DottedName
import loci.registry.Binding
import kofre.dotted.Dotted
import rescala.default.{Events, Evt, Signal}
import com.github.plokhotnyuk.jsoniter_scala.core.{readFromString, writeToString}
import webapp.Codecs.{replicaID, given}
import loci.serializer.jsoniterScala.given
import concurrent.ExecutionContext.Implicits.global

case class EventedCounter(
    signal: rescala.default.Signal[DeltaBufferRDT[PosNegCounter]],
    incrementValueEvent: rescala.default.Evt[Int],
)

object CounterService {
  val counter = createCounterRef()

  def createCounterRef(): Future[EventedCounter] = {
    // restore counter from indexeddb
    val init: Future[PosNegCounter] =
      typings.idbKeyval.mod
        .get[scala.scalajs.js.Object]("counter")
        .toFuture
        .map(value =>
          value.toOption
            .map(value => readFromString[PosNegCounter](JSON.stringify(value)))
            .getOrElse(PosNegCounter.zero),
        );

    init.map(init => {
      // a last writer wins register. This means the last value written is the actual value.
      val lastWriterWins = DeltaBufferRDT(replicaID, init)

      // event that fires when the user wants to change the value
      val testChangeEvent = rescala.default.Evt[Int]();

      // event that fires when changes from other peers are received
      val deltaEvent = Evt[DottedName[PosNegCounter]]()

      // look at foldAll documentation+example
      val counterSignal: Signal[DeltaBufferRDT[PosNegCounter]] = Events.foldAll(lastWriterWins)(current => {
        Seq(
          // if the user changes the value, update the register with the new value
          testChangeEvent.act2({ v =>
            {
              current.resetDeltaBuffer().add(v)
            }
          }),
          // if we receive a delta from a peer, apply it
          deltaEvent.act2({ delta => current.resetDeltaBuffer().applyDelta(delta) }),
        )
      })

      counterSignal.observe(
        value => {
          // this is async which means this is not robust
          typings.idbKeyval.mod.set("counter", JSON.parse(writeToString(value.state.store)))
        },
        fireImmediately = true,
      )

      WebRTCService.distributeDeltaCRDT(counterSignal, deltaEvent, WebRTCService.registry)(
        Binding[Dotted[PosNegCounter] => Unit]("counter"),
      )

      EventedCounter(counterSignal, testChangeEvent)
    })
  }
}
