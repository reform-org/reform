package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonReader, JsonValueCodec, JsonWriter}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import kofre.base.Defs
import kofre.datatypes.{RGA, TimedVal}
import kofre.time.Dot
import kofre.decompose.containers.DeltaBufferRDT
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.dotted.{DotFun, Dotted}
import loci.transmitter.IdenticallyTransmittable

import scala.annotation.nowarn
import rescala.default.*
import java.util.concurrent.ThreadLocalRandom
import kofre.datatypes.PosNegCounter

// Supporting code to serialize and deserialize objects
object Codecs {

  // every client has an id
  val replicaID: String = ThreadLocalRandom.current().nextLong().toHexString

  implicit val transmittableProject: IdenticallyTransmittable[Dotted[webapp.Project]] =
    IdenticallyTransmittable()

  implicit val transmittablePositiveNegativeCounter: IdenticallyTransmittable[Dotted[PosNegCounter]] =
    IdenticallyTransmittable()

  implicit val codecDottedPositiveNegativeCounter: JsonValueCodec[Dotted[kofre.datatypes.PosNegCounter]] =
    JsonCodecMaker.make

  implicit val codecProject: JsonValueCodec[Dotted[webapp.Project]] =
    JsonCodecMaker.make

  implicit val codecDot3: JsonValueCodec[Dot] =
    JsonCodecMaker.make

  implicit val codecDot2: JsonValueCodec[DotFun[String]] =
    JsonCodecMaker.make

  implicit val codecDot5: JsonValueCodec[String] =
    JsonCodecMaker.make

  implicit val codedDot6: JsonValueCodec[kofre.datatypes.TimedVal[String]] = JsonCodecMaker.make

  implicit val test: JsonValueCodec[List[(Dot, String)]] = mapEntriesSerializer[String]

  implicit val test2: JsonValueCodec[List[(Dot, TimedVal[String])]] = mapEntriesSerializer[TimedVal[String]]

  implicit def mapEntriesSerializer[A >: scala.Nothing <: scala.Any](implicit
      test: JsonValueCodec[A],
  ): JsonValueCodec[List[(Dot, A)]] =
    JsonCodecMaker.make

  implicit def mapSerializer[A >: scala.Nothing <: scala.Any](implicit
      test: JsonValueCodec[A],
  ): JsonValueCodec[Map[Dot, A]] =
    new JsonValueCodec[Map[Dot, A]] {
      override def decodeValue(
          in: JsonReader,
          default: Map[Dot, A],
      ): Map[Dot, A] = {
        Map.from(mapEntriesSerializer.decodeValue(in, default.toList))
      }
      override def encodeValue(x: Map[Dot, A], out: JsonWriter): Unit =
        mapEntriesSerializer.encodeValue(x.toList, out)

      override def nullValue: Map[kofre.time.Dot, A] = Map.empty
    }

  implicit val codecDeltaBufferPositiveNegativeCounter: JsonValueCodec[DeltaBufferRDT[PosNegCounter]] =
    new JsonValueCodec[DeltaBufferRDT[PosNegCounter]] {
      override def decodeValue(
          in: JsonReader,
          default: DeltaBufferRDT[PosNegCounter],
      ): DeltaBufferRDT[PosNegCounter] = {
        val state: Dotted[PosNegCounter] = codecDottedPositiveNegativeCounter.decodeValue(in, default.state)
        new DeltaBufferRDT[PosNegCounter](state, replicaID, List())
      }
      override def encodeValue(x: DeltaBufferRDT[PosNegCounter], out: JsonWriter): Unit =
        codecDottedPositiveNegativeCounter.encodeValue(x.state, out)
      override def nullValue: DeltaBufferRDT[PosNegCounter] = {
        DeltaBufferRDT(replicaID, PosNegCounter.zero)
      }
    }

  implicit val codecPositiveNegativeCounter: JsonValueCodec[PosNegCounter] = JsonCodecMaker.make
}
