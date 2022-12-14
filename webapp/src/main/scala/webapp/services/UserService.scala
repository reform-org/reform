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
import webapp.Codecs.{myReplicaID, given}
import loci.serializer.jsoniterScala.given
import concurrent.ExecutionContext.Implicits.global
import webapp.npm.IdbKeyval
import webapp.User
import scala.collection.mutable

case class EventedUser(
    id: String,
    signal: rescala.default.Signal[User],
    changeEvent: rescala.default.Evt[User => User],
)

object UserService {

  private val userMap: mutable.Map[String, Future[EventedUser]] = mutable.Map.empty
  def createOrGetUser(id: String): Future[EventedUser] = {
     userMap.getOrElseUpdate(id, createUserRef(id))
  }

  // // TODO FIXME for User creation this could non non-async? Or should it write into the database at creation? Or does this simply create too complex code?
  private def createUserRef(id: String): Future[EventedUser] = {
     // restore from indexeddb
    val init: Future[User] = IdbKeyval
      .get[scala.scalajs.js.Object](s"user-$id")
      .toFuture
      .map(value =>
        value.toOption
          .map(value => readFromString[User](JSON.stringify(value)))
          .getOrElse(User.empty),
      );

     init.map(init => {
       val user = init

       // event that fires when the user wants to change the value
       val changeEvent = rescala.default.Evt[User => User]();

       // event that fires when changes from other peers are received
       val deltaEvent = Evt[User]()

       // look at foldAll documentation+example
       val userSignal: Signal[User] = Events.foldAll(user)(current => {
         Seq(
           // if the user wants to increase the value, update the register accordingly
           changeEvent.act2(_(current)),
           // if we receive a delta from a peer, apply it
           deltaEvent.act2(delta => {
             println("apply delta")
             current.merge(delta)
           }),
         )
       })

       userSignal.observe(
         value => {
           org.scalajs.dom.console.log(value)
           // write the updated value to persistent storage
           // TODO FIXME this is async which means this is not robust
           IdbKeyval.set(s"user-$id", JSON.parse(writeToString(value)))
         },
         fireImmediately = true,
       )

       WebRTCService.distributeDeltaCRDT(userSignal, deltaEvent, WebRTCService.registry)(
         Binding[User => Unit](s"user-$id"),
       )

       EventedUser(id, userSignal, changeEvent)
     })

  }
}