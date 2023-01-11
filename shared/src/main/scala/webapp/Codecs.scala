package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.datatypes.PosNegCounter
import loci.transmitter.IdenticallyTransmittable
import webapp.webrtc.DeltaFor

import java.util.UUID

// Supporting code to serialize and deserialize objects
// TODO: Codecs should be declared where they are used
object Codecs {

  // every client has an id
  val myReplicaID: String = UUID.randomUUID().toString

  implicit def identicallyTransmittable[A]: IdenticallyTransmittable[A] = IdenticallyTransmittable()

  implicit val codecPositiveNegativeCounter: JsonValueCodec[PosNegCounter] = JsonCodecMaker.make

  implicit val codecDeltaForPositiveNegativeCounter: JsonValueCodec[DeltaFor[PosNegCounter]] = JsonCodecMaker.make

  implicit val codecDeltaForGrowOnlySetString: JsonValueCodec[DeltaFor[GrowOnlySet[String]]] = JsonCodecMaker.make
}
