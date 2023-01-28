package webapp.pages

import webapp.services.Page
import webapp.Services
import outwatch.VNode
import rescala.default.Var
import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.given
import outwatch.*
import outwatch.dsl.*

case class WebRTCCompression() extends Page {
  private val offers = Var("")
  private val count = Var(0)

  private val codec: JsonValueCodec[webrtc.WebRTC.CompleteSession] = JsonCodecMaker.make

  def magic(using services: Services): Unit = {
    val pendingConnection = webrtcIntermediate(WebRTC.offer())
    services.webrtc.registry.connect(pendingConnection.connector)
    pendingConnection.session.map(session => {
      count.transform(value => {
        if (value < 399) {
          magic
        }
        value + 1
      })
      offers.transform(v =>
          v + writeToString(session)(codec) + "\n"
      )
    })
  }

  override def render(using services: Services): VNode = {
    magic
    div(
      pre(
          idAttr := "webrtc-compression-count",
          count
      ),
      pre(
          idAttr := "webrtc-compression",
          offers
      )
    )
  }
}