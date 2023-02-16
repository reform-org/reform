package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import kofre.base.*
import loci.registry.Registry
import rescala.default.*
import webapp.*
import webapp.webrtc.*

// TODO FIXME integrate in ReplicationGroup
case class Syncer[A](name: String)(using
    registry: Registry,
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
) {

  private val replicator = ReplicationGroup(name)

  def sync(storage: Storage[A], id: String, value: A): Synced[A] = {
    var synced = Synced(storage, id, Var(value))

    replicator.distributeDeltaRDT(id, synced)

    synced
  }
}
