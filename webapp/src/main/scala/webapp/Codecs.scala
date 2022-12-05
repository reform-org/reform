package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonReader, JsonValueCodec, JsonWriter}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import kofre.base.Defs
import kofre.datatypes.{RGA, TimedVal}
import kofre.time.Dot
import kofre.decompose.containers.DeltaBufferRDT
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.decompose.interfaces.GListInterface.GList
import kofre.decompose.interfaces.GListInterface.GListNode
import kofre.decompose.interfaces.GListInterface.GListElem
import kofre.dotted.{DotFun, Dotted}
import loci.transmitter.IdenticallyTransmittable

import scala.annotation.nowarn
import rescala.default.*
import java.util.concurrent.ThreadLocalRandom
import kofre.datatypes.PosNegCounter
import java.util.UUID

// Supporting code to serialize and deserialize objects
object Codecs {

  // every client has an id
  val replicaID: String = UUID.randomUUID().toString()

  implicit def identicallyTransmittable[A]: IdenticallyTransmittable[A] = IdenticallyTransmittable()

  implicit val codecDottedPositiveNegativeCounter: JsonValueCodec[Dotted[PosNegCounter]] = JsonCodecMaker.make

  implicit val codecPositiveNegativeCounter: JsonValueCodec[PosNegCounter] = JsonCodecMaker.make

  implicit val codecDottedProject: JsonValueCodec[Dotted[Project]] =
    JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  @nowarn()
  implicit val codecDottedRGAString: JsonValueCodec[Dotted[RGA[String]]] =
    JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

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
}
