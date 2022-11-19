package webapp.pages

import loci.registry.Registry
import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import org.scalajs.dom
import outwatch._
import outwatch.dsl._
import rescala.default._
import webapp.services._
import webapp._
import webapp.given
import cats.effect.SyncIO
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import org.scalajs.dom.UIEvent
import com.github.plokhotnyuk.jsoniter_scala.core._

case class WebRTCHandling() extends Page {
  private val codec: JsonValueCodec[webrtc.WebRTC.CompleteSession] = JsonCodecMaker.make
  private val registry                                             = new Registry
  private val sessionOutput                                        = Var("")
  private val sessionInput                                         = Var("")
  private var hostedSessionConnector: Option[WebRTC.Connector]     = None

  private def connected(): Unit = {
    sessionOutput.set("")
    sessionInput.set("")
  }

  private def showSession(s: WebRTC.CompleteSession): Unit = {
    val message = writeToString(s)(codec)
    sessionOutput.set(message)
  }

  def render(using services: Services): VNode = {
    div(
      p(pre(sessionOutput)),
      p(
        textArea(
          sessionInput,
          onInput.value --> sessionInput,
        ),
      ),
      div(
        button(
          "host",
          onClick.foreach(_ => hostSession()),
        ),
        button(
          "connect to host",
          onClick.foreach(_ => connectToHost()),
        ),
        button(
          "confirm connection to client",
          onClick.foreach(_ => confirmConnectionToClient()),
        ),
      ),
    )
  }

  private def confirmConnectionToClient(): Unit = {
    val connectionString = readFromString(sessionInput.now)(codec)
    hostedSessionConnector match
      case None            => throw IllegalStateException("No session is being hosted")
      case Some(connector) => connector.set(connectionString)
  }

  private def connectToHost(): Unit = {
    val connectionString = readFromString(sessionInput.now)(codec)
    val res              = webrtcIntermediate(WebRTC.answer())
    res.session.foreach(showSession)
    registry.connect(res.connector).foreach(_ => connected())
    res.connector.set(connectionString)
  }

  private def hostSession(): Unit = {
    val res = webrtcIntermediate(WebRTC.offer())
    res.session.foreach(showSession)
    hostedSessionConnector = Some(res.connector)
    registry.connect(res.connector).foreach(_ => connected())
  }

  case class PendingConnection(connector: WebRTC.Connector, session: Future[WebRTC.CompleteSession])

  private def webrtcIntermediate(cf: ConnectorFactory) = {
    val p      = Promise[WebRTC.CompleteSession]()
    val answer = cf complete p.success
    PendingConnection(answer, p.future)
  }
}
