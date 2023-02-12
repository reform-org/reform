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

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import com.github.plokhotnyuk.jsoniter_scala.core.*
import loci.communicator.Connector
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import loci.registry.*
import loci.transmitter.RemoteRef
import org.scalajs.dom
import org.scalajs.dom.{console, window}
import rescala.default.*
import webapp.*
import webapp.npm.Utils
import webapp.utils.Base64
import scala.util.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.annotation.nowarn

import loci.serializer.jsoniterScala.given
import loci.communicator.ws.webnative.WS

class ConnectionInformation(val session: WebRTC.CompleteSession, val alias: String, val source: String = "manual") {}
class StoredConnectionInformation(
    val alias: String,
    val source: String = "manual",
    val uuid: String = "",
    val connectionId: String = "",
) {} // different object for discovery and manual

case class PendingConnection(
    connector: WebRTC.Connector,
    session: Future[ConnectionInformation],
    connection: dom.RTCPeerConnection,
)
object PendingConnection {
  def webrtcIntermediate(cf: ConnectorFactory, alias: String) = {
    val p = Promise[ConnectionInformation]()
    val answer = cf.complete(s => p.success(new ConnectionInformation(s, alias)): @nowarn("msg=discarded expression"))
    PendingConnection(answer, p.future, answer.connection)
  }
  private val codec: JsonValueCodec[ConnectionInformation] = JsonCodecMaker.make
  def sessionAsToken(s: ConnectionInformation) = Base64.encode(writeToString(s)(codec))

  def tokenAsSession(s: String) = readFromString(Base64.decode(s))(codec)
}

class WebRTCService(using registry: Registry) {

  private var connectionInfo = Map[RemoteRef, StoredConnectionInformation]()
  private var webRTCConnections = Map[RemoteRef, dom.RTCPeerConnection]() // could merge this map with the one above
  private var connectionRefs = Map[String, RemoteRef]()

  private val removeConnection = Evt[RemoteRef]()
  private val addConnection = Evt[RemoteRef]()
  private val addConnectionB = addConnection.act(current[Seq[RemoteRef]] :+ _)
  private val removeConnectionB = removeConnection.act(r => current[Seq[RemoteRef]].filter(b => !b.equals(r)))

  val connections = Fold(Seq.empty: Seq[RemoteRef])(addConnectionB, removeConnectionB)

  registry.connect(WS("ws://localhost:1334/registry/")): @nowarn

  def registerConnection(
      connector: Connector[Connections.Protocol],
      alias: Future[String],
      source: String,
      connection: dom.RTCPeerConnection,
      uuid: String = "",
      connectionId: String = "",
  ): Future[RemoteRef] = {
    registry
      .connect(connector)
      .andThen(r => {
        alias.onComplete(alias => {
          connectionInfo += (r.get -> StoredConnectionInformation(alias.get, source, uuid, connectionId))
          connectionRefs += (connectionId -> r.get)
          webRTCConnections += (r.get -> connection)
        })
      })
      .andThen(r => {
        addConnection.fire(r.get)
      })
  }

  def closeConnectionById(id: String) = {
    connectionRefs.get(id) match {
      case None      => {}
      case Some(ref) => ref.disconnect()
    }
  }

  def getInformation(ref: RemoteRef): StoredConnectionInformation = {
    connectionInfo.get(ref).getOrElse(StoredConnectionInformation("Anonymous", "unknown"))
  }

  def getConnectionMode(ref: RemoteRef): Future[String] = {
    val connection = webRTCConnections.get(ref).get

    Utils.usesTurn(connection).map(usesTurn => if (usesTurn) "relay" else "direct")
  }
}
