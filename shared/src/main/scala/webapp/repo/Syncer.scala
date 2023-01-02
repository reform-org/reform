package webapp.repo

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import kofre.base.*
import loci.registry.Binding
import webapp.*
import rescala.default.*
import webapp.services.WebRTCService

import loci.serializer.jsoniterScala.given
import webapp.Codecs.given
import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
import webapp.npm.IndexedDB
import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global

case class Syncer[A](name: String, defaultValue: A)(using
    dcl: DecomposeLattice[A],
    bottom: Bottom[A],
    codec: JsonValueCodec[A],
) {

  private val binding = Binding[DeltaFor[A] => Unit](name)

  private val replicator = ReplicationGroup(rescala.default, WebRTCService.registry, binding)

  private val cache: mutable.Map[String, Synced[A]] = mutable.Map.empty

  def getOrDefault(id: String): Synced[A] = {
    if (cache.contains(id)) {
      return cache(id)
    }

    val deltaEvents = TwoWayDeltaEvents()

    val outerSignal: Signal[A] = Signals
      .fromFuture(
        IndexedDB
          .get[A](getKey(id))
          .map(future =>
          future.map(value => {

            val signal: Signal[A] = deltaEvents.mergeAllDeltas(value)

            replicator.distributeDeltaRDT(id, signal, deltaEvents.incomingDeltaEvent)

            signal
          })),
      )
      .flatten

    val synced = Synced(id, outerSignal, deltaEvents.outgoingDeltaEvent)

    synced.signal.observe(value => IndexedDB
      .set(getKey(synced.id), value))

    cache.put(id, synced)

    synced
  }

  private def getKey(id: String): String = s"$name-$id"

  private class TwoWayDeltaEvents {

    val outgoingDeltaEvent: Evt[A => A] = Evt()

    val incomingDeltaEvent: Evt[A] = Evt()

    private def applyOutgoingDelta(current: A) =
      outgoingDeltaEvent.act2(_(current))

    private def applyIncomingDelta(current: A) =
      incomingDeltaEvent.act2(delta => current.merge(delta))

    def mergeAllDeltas(value: A): Signal[A] =
      Events.foldAll(value)(current => Seq(applyOutgoingDelta(current), applyIncomingDelta(current)))

  }
}
