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
import rescala.default.*
import webapp.*
import webapp.utils.Base64
import webapp.npm.JSUtils

import outwatch.*
import outwatch.dsl.*

import webapp.given_ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.annotation.nowarn
import webapp.services.{ToastMode, ToastType, Toaster}
import loci.communicator.ws.webnative.WS
import loci.communicator.broadcastchannel.BroadcastChannel
import org.scalajs.dom.RTCPeerConnection
import webapp.utils.Futures.*
import scala.util.Try
import webapp.services.DiscoveryService

class ConnectionInformation(val session: WebRTC.CompleteSession, val alias: String, val source: String = "manual") {}
class StoredConnectionInformation(
    var alias: String,
    val source: String = "manual",
    val uuid: String = "",
    val displayId: String = "",
    val connectionId: String = "",
) {} // different object for discovery and manual

case class PendingConnection(
    connector: WebRTC.Connector,
    session: Future[ConnectionInformation],
    connection: RTCPeerConnection,
)
object PendingConnection {
  def webrtcIntermediate(cf: ConnectorFactory, alias: String): PendingConnection = {
    val p = Promise[ConnectionInformation]()
    val answer = cf.complete(s => p.success(new ConnectionInformation(s, alias)))
    PendingConnection(answer, p.future, answer.connection)
  }
  private val codec: JsonValueCodec[ConnectionInformation] = JsonCodecMaker.make: @nowarn
  def sessionAsToken(s: ConnectionInformation): String = Base64.encode(writeToString(s)(codec))

  def tokenAsSession(s: String): ConnectionInformation = readFromString(Base64.decode(s))(codec)
}

class WebRTCService(using registry: Registry, toaster: Toaster, discovery: DiscoveryService) {

  private var connectionInfo = Map[RemoteRef, StoredConnectionInformation]()
  private var webRTCConnections = Map[RemoteRef, RTCPeerConnection]() // could merge this map with the one above
  private var connectionRefs = Map[String, RemoteRef]()

  private val removeConnection = Evt[RemoteRef]()
  private val addConnection = Evt[RemoteRef]()
  private val addConnectionB = addConnection.act(current[Seq[RemoteRef]] :+ _)
  private val removeConnectionB = removeConnection.act(r => current[Seq[RemoteRef]].filter(b => !b.equals(r)))

  val connections: Signal[Seq[RemoteRef]] = Fold(Seq.empty: Seq[RemoteRef])(addConnectionB, removeConnectionB)

  def registerConnection(
      connector: Connector[Connections.Protocol],
      alias: String,
      source: String,
      connection: RTCPeerConnection,
      uuid: String = "",
      displayId: String = "",
      connectionId: String = "",
      onConnected: (ref: RemoteRef) => Unit = (_) => {},
  ): Future[RemoteRef] = {
    registry
      .connect(connector)
      .andThen(r => {
        val storedConnection = StoredConnectionInformation(alias, source, uuid, displayId, connectionId)
        connectionInfo += (r.get -> storedConnection)
        connectionRefs += (connectionId -> r.get)
        webRTCConnections += (r.get -> connection)

        onConnected(r.get)

        addConnection.fire(r.get)

        toaster.make(span(b(storedConnection.alias), " has just joined! 🚀"), ToastMode.Short, ToastType.Success)
      })
  }

  def closeConnectionById(id: String): Unit = {
    connectionRefs.get(id) match {
      case None =>
      case Some(ref) => {
        ref.disconnect()
        discovery.reportClosedConnection(id)
      }
    }
  }

  def getInformation(ref: RemoteRef): StoredConnectionInformation = {
    connectionInfo.getOrElse(ref, StoredConnectionInformation("Anonymous", "unknown"))
  }

  def setAlias(ref: RemoteRef, alias: String): Unit = {
    connectionInfo = connectionInfo.transform((r, storedConnection) => {
      if (ref == r) {
        storedConnection.alias = alias
      }
      storedConnection
    })
  }

  def getConnectionMode(ref: RemoteRef): Future[String] = {
    val connection = webRTCConnections(ref)

    JSUtils.usesTurn(connection).map(usesTurn => if (usesTurn) "relay" else "direct")
  }

  registry.remoteLeft.monitor(remoteRef => {
    val connectionInfo = getInformation(remoteRef);
    toaster.make(span(b(connectionInfo.alias), " has left! 👋"), ToastMode.Short, ToastType.Default)

    removeConnection.fire(remoteRef)
  })

  registry.connect(BroadcastChannel("default"))
}
