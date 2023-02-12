package webapp.webrtc

import loci.registry.Registry
import loci.transmitter.RemoteRef
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.registry.Binding
import scala.util.Success
import scala.util.Failure
import scala.annotation.nowarn
import concurrent.ExecutionContext.Implicits.global
import java.util.Timer
import java.util.TimerTask
import loci.serializer.jsoniterScala.given

class PingService(using registry: Registry) {

  implicit val codec: JsonValueCodec[String] = JsonCodecMaker.make
  val binding = Binding[String => Unit]("pings")

  private def ping(timer: Timer, ref: RemoteRef) = {
    if (!ref.connected) {
      timer.cancel()
    }
    val remoteUpdate = registry.lookup(binding, ref)
    remoteUpdate("pingdata").onComplete {
      case Success(_) => println("update ping success")
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
  }): @nowarn("msg=discarded expression")

  registry.bindSbj(binding) { (_: RemoteRef, _: String) => {} }
}
