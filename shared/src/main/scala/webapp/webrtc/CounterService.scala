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
package webapp.webrtc

import kofre.datatypes.PosNegCounter
import kofre.syntax.PermIdMutate
import rescala.default.Evt
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import rescala.default.*
import webapp.npm.IndexedDB
import webapp.Codecs.*

case class EventedCounter(
    signal: rescala.default.Signal[PosNegCounter],
    incrementValueEvent: rescala.default.Evt[Int],
)

object CounterService {

  val counter = createCounterRef()

  def createCounterRef(): Future[EventedCounter] = {
    // restore from indexeddb
    val init: Future[PosNegCounter] =
      IndexedDB
        .get[PosNegCounter]("counter")
        .map(option => option.getOrElse(PosNegCounter.zero))

    init.map(init => {
      val positiveNegativeCounter = init

      // event that fires when the user wants to change the value
      val changeEvent = rescala.default.Evt[Int]()

      // event that fires when changes from other peers are received
      val deltaEvent = Evt[PosNegCounter]()

      // look at foldAll documentation+example
      val counterSignal: Signal[PosNegCounter] = Events.foldAll(positiveNegativeCounter)(current => {
        Seq(
          // if the user wants to change the value, update the register accordingly
          changeEvent.act2({ v =>
            {
              current.add(v)(using PermIdMutate.withID(myReplicaID))
            }
          }),
          // if we receive a delta from a peer, apply it
          deltaEvent.act2({ delta => current.merge(delta) }),
        )
      })

      counterSignal.observe(
        value => {
          // write the updated value to persistent storage
          // TODO FIXME this is async which means this is not robust
          IndexedDB.set("counter", value)
        },
        fireImmediately = true,
      )

      WebRTCService.counterReplicator.distributeDeltaRDT("counter", counterSignal, deltaEvent)

      EventedCounter(counterSignal, changeEvent)
    })
  }
}
