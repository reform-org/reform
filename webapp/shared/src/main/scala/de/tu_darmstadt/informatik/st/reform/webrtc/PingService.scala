package de.tu_darmstadt.informatik.st.reform.webrtc

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import loci.registry.Binding
import loci.registry.Registry
import loci.serializer.jsoniterScala.given
import loci.transmitter.RemoteRef

import java.util.Timer
import java.util.TimerTask
import scala.util.Failure
import scala.util.Success

class PingService(using registry: Registry) {

  implicit val codec: JsonValueCodec[String] = JsonCodecMaker.make
  val binding = Binding[String => Unit](s"pings")

  private def ping(timer: Timer, ref: RemoteRef) = {
    if (!ref.connected) {
      timer.cancel()
    }
    val remoteUpdate = registry.lookup(binding, ref)
    remoteUpdate("pingdata").onComplete {
      case Success(_) => {}
      case Failure(_) => println("update ping failure")
    }
  }

  registry.remoteJoined.foreach(remoteRef => {
    val timer = new Timer()
    timer.schedule(
      new TimerTask {
        def run() = ping(timer, remoteRef)
      },
      0,
      10000,
    )
  })

  registry.bindSbj(binding) { (_: RemoteRef, _: String) => {} }
}
