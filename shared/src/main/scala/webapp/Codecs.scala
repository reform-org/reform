package webapp

import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.datatypes.PosNegCounter
import loci.transmitter.IdenticallyTransmittable
import webapp.webrtc.DeltaFor

import java.util.UUID
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonReader, JsonValueCodec, JsonWriter}
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig

// Supporting code to serialize and deserialize objects
// TODO: Codecs should be declared where they are used
object Codecs {

  // every client has an id
  val myReplicaID: kofre.base.Id = kofre.base.Id.gen()

  implicit def identicallyTransmittable[A]: IdenticallyTransmittable[A] = IdenticallyTransmittable()

  implicit val idCodec: JsonValueCodec[kofre.base.Id] = JsonCodecMaker.make[String].asInstanceOf

  implicit val codecPositiveNegativeCounter: JsonValueCodec[PosNegCounter] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val codecDeltaForPositiveNegativeCounter: JsonValueCodec[DeltaFor[PosNegCounter]] = JsonCodecMaker.make

  implicit val codecDeltaForGrowOnlySetString: JsonValueCodec[DeltaFor[GrowOnlySet[String]]] = JsonCodecMaker.make
}
