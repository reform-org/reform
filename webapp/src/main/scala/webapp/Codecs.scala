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

object Codecs {

  // every client has an id
  val replicaID: String = ThreadLocalRandom.current().nextLong().toHexString

  // TaskData = Int

  implicit val transmittableLWW: IdenticallyTransmittable[Dotted[LWWRegister[Int]]] =
    IdenticallyTransmittable()

  implicit val dotKeyCodec: JsonKeyCodec[Dot] = new JsonKeyCodec[Dot] {
    override def decodeKey(in: JsonReader): Dot = {
      val Array(time, id) = in.readKeyAsString().split("-", 2)
      Dot(id, time.toLong)
    }
    override def encodeKey(x: Dot, out: JsonWriter): Unit = out.writeKey(s"${x.time}-${x.replicaId}")
  }

  implicit val codecLwwState: JsonValueCodec[Dotted[DotFun[TimedVal[Int]]]] = JsonCodecMaker.make

  type LwC = DeltaBufferRDT[LWWRegister[Int]]
  implicit val codecLww: JsonValueCodec[LwC] =
    new JsonValueCodec[LwC] {
      override def decodeValue(in: JsonReader, default: LwC): LwC = {
        val state: Dotted[LWWRegister[Int]] = codecLwwState.decodeValue(in, default.state)
        new DeltaBufferRDT[LWWRegister[Int]](state, replicaID, List())
      }
      override def encodeValue(x: LwC, out: JsonWriter): Unit = codecLwwState.encodeValue(x.state, out)
      override def nullValue: LwC = {
        println(s"reading null")
        DeltaBufferRDT(replicaID, LWWRegisterInterface.empty[Int])
      }
    }

  implicit val todoTaskCodec: JsonValueCodec[Int] = JsonCodecMaker.make
  /*
  implicit val taskRefCodec: JsonValueCodec[TaskRef] = JsonCodecMaker.make

  @nowarn()
  implicit val codecState: JsonValueCodec[Dotted[RGA[TaskRef]]] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
  implicit val codecRGA: JsonValueCodec[DeltaBufferRDT[RGA[TaskRef]]] =
    new JsonValueCodec[DeltaBufferRDT[RGA[TaskRef]]] {
      override def decodeValue(
          in: JsonReader,
          default: DeltaBufferRDT[RGA[TaskRef]]
      ): DeltaBufferRDT[RGA[TaskRef]] = {
        val state = codecState.decodeValue(in, default.state)
        new DeltaBufferRDT[RGA[TaskRef]](state, replicaId, List())
      }
      override def encodeValue(x: DeltaBufferRDT[RGA[TaskRef]], out: JsonWriter): Unit =
        codecState.encodeValue(x.state, out)
      override def nullValue: DeltaBufferRDT[RGA[TaskRef]] = DeltaBufferRDT(replicaId, RGA.empty[TaskRef])
    }

  implicit val transmittableList: IdenticallyTransmittable[Dotted[RGA[TaskRef]]] =
    IdenticallyTransmittable()
   */

}
