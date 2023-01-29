package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.transmitter.IdenticallyTransmittable

// Supporting code to serialize and deserialize objects
// TODO: Codecs should be declared where they are used
object Codecs {

  // every client has an id
  val myReplicaID: kofre.base.Id = kofre.base.Id.gen()

  implicit def identicallyTransmittable[A]: IdenticallyTransmittable[A] = IdenticallyTransmittable()

  implicit val idCodec: JsonValueCodec[kofre.base.Id] = JsonCodecMaker.make[String].asInstanceOf
}
