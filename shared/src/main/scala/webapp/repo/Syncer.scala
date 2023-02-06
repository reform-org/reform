package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import kofre.base.*
import loci.registry.Registry
import rescala.default.*
import webapp.*
import webapp.webrtc.*

case class Syncer[A](name: String)(using
    registry: Registry,
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
) {

  private val replicator = ReplicationGroup(name)

  def sync(storage: Storage[A], id: String, value: A): Synced[A] = {

    val deltaEvents = TwoWayDeltaEvents()

    val signal: Signal[A] = deltaEvents.mergeAllDeltas(value)

    replicator.distributeDeltaRDT(id, signal, deltaEvents.deltaEvent)

    Synced(storage, id, signal, deltaEvents.deltaEvent)
  }

  private class TwoWayDeltaEvents {

    val deltaEvent: Evt[A => A] = Evt()
    val deltaEventAct = deltaEvent.act(v => v(current))

    def mergeAllDeltas(value: A): Signal[A] =
      Fold(value)(deltaEventAct)
  }
}
