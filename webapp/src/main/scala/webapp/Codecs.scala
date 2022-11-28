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

object Codecs {

  // every client has an id
  val replicaID: String = ThreadLocalRandom.current().nextLong().toHexString

  implicit val transmittableLWW: IdenticallyTransmittable[Dotted[PosNegCounter]] =
    IdenticallyTransmittable()

  implicit val dotKeyCodec: JsonKeyCodec[Dot] = new JsonKeyCodec[Dot] {
    override def decodeKey(in: JsonReader): Dot = {
      val Array(time, id) = in.readKeyAsString().split("-", 2)
      Dot(id, time.toLong)
    }
    override def encodeKey(x: Dot, out: JsonWriter): Unit = out.writeKey(s"${x.time}-${x.replicaId}")
  }

  implicit val codecLwwState: JsonValueCodec[Dotted[kofre.datatypes.PosNegCounter]] = JsonCodecMaker.make

  type LwC = DeltaBufferRDT[PosNegCounter]
  implicit val codecLww: JsonValueCodec[LwC] = new JsonValueCodec[LwC] {
    override def decodeValue(in: JsonReader, default: LwC): LwC = {
      val state: Dotted[PosNegCounter] = codecLwwState.decodeValue(in, default.state)
      new DeltaBufferRDT[PosNegCounter](state, replicaID, List())
    }
    override def encodeValue(x: LwC, out: JsonWriter): Unit = codecLwwState.encodeValue(x.state, out)
    override def nullValue: LwC = {
      println(s"reading null")
      DeltaBufferRDT(replicaID, PosNegCounter.zero)
    }
  }

  implicit val codecPosNegCounter: JsonValueCodec[PosNegCounter] = JsonCodecMaker.make

  implicit val todoTaskCodec: JsonValueCodec[Int] = JsonCodecMaker.make
}
