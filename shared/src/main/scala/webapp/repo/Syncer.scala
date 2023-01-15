package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import kofre.base.*
import loci.registry.Binding
import webapp.*
import rescala.default.*
import webapp.Codecs.*
import loci.serializer.jsoniterScala.given
import webapp.webrtc.*

case class Syncer[A](name: String)(using
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
) {

  private val replicator = ReplicationGroup(name)

  def sync(id: String, value: A): Synced[A] = {

    val deltaEvents = TwoWayDeltaEvents()

    val signal: Signal[A] = deltaEvents.mergeAllDeltas(value)

    replicator.distributeDeltaRDT(id, signal, deltaEvents.incomingDeltaEvent)

    Synced(id, signal, deltaEvents.outgoingDeltaEvent)
  }

  private class TwoWayDeltaEvents {

    val outgoingDeltaEvent: Evt[A => A] = Evt()

    val incomingDeltaEvent: Evt[A] = Evt()

    def mergeAllDeltas(value: A): Signal[A] =
      Events.foldAll(value) { current =>
        Seq(
          outgoingDeltaEvent.act2 { function =>
            println(s"old: $current, new: ${function(current)}")
            function(current)
          },
          incomingDeltaEvent.act2 { delta =>
            println(s"merge, current: $current, delta: $delta, new: ${current.merge(delta)}")
            current.merge(delta)
          },
        )
      }
  }
}
