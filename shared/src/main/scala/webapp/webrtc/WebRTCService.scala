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
package webapp.webrtc

import kofre.datatypes.PosNegCounter
import loci.registry.*
import webapp.*
import webapp.Codecs.*
import rescala.default.*
import org.scalajs.dom.window
import org.scalajs.dom
import loci.transmitter.RemoteRef
import loci.communicator.webrtc.WebRTC
import loci.communicator.Connector
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ConnectionInformation(val session: WebRTC.CompleteSession, val alias: String, val source: String = "manual") {}
class StoredConnectionInformation(val alias: String, val source: String = "manual") {}
object WebRTCService {
  val registry: Registry = new Registry

  private val connectionInfo = scala.collection.mutable.Map[RemoteRef, StoredConnectionInformation]()

  private val removeConnection = Evt[RemoteRef]()
  private val addConnection = Evt[RemoteRef]()
  private val addConnectionB = addConnection.act(current[Seq[RemoteRef]] :+ _)
  private val removeConnectionB = removeConnection.act(r => current[Seq[RemoteRef]].filter(b => !b.equals(r)))

  val connections = Fold(Seq.empty: Seq[RemoteRef])(addConnectionB, removeConnectionB)

  private val setOnlineStatus = Evt[Boolean]()
  private val setOnlineStatusB = setOnlineStatus.act(identity)

  val online = Fold(window.navigator.onLine: Boolean)(setOnlineStatusB)

  def registerConnection(
      connector: Connector[Connections.Protocol],
      alias: Future[String],
      source: String,
  ): Future[RemoteRef] = {
    registry
      .connect(connector)
      .andThen(r => {
        alias.onComplete(alias => {
          connectionInfo += (r.get -> StoredConnectionInformation(alias.get, source))
        })
      })
      .andThen(r => {
        addConnection.fire(r.get)
      })
  }

  def getInformation(ref: RemoteRef): StoredConnectionInformation = {
    connectionInfo.get(ref).getOrElse(StoredConnectionInformation("Anonymous", "manual"))
  }

  // registry.remoteJoined.monitor(addConnection.fire)
  registry.remoteLeft.monitor(removeConnection.fire)
  window.addEventListener("online", { (e: dom.Event) => setOnlineStatus.fire(true) })
  window.addEventListener("offline", { (e: dom.Event) => setOnlineStatus.fire(false) })
}
