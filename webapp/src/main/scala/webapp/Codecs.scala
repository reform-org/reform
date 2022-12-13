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
  val myReplicaID: String = UUID.randomUUID().toString()

  implicit def identicallyTransmittable[A]: IdenticallyTransmittable[A] = IdenticallyTransmittable()

  implicit val codecPositiveNegativeCounter: JsonValueCodec[PosNegCounter] = JsonCodecMaker.make
}
