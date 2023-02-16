package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

object BasicCodecs {

  // every client has an id
  val myReplicaID: kofre.base.Uid = kofre.base.Uid.gen()

  implicit val stringCodec: JsonValueCodec[String] = JsonCodecMaker.make

  implicit val idCodec: JsonValueCodec[kofre.base.Uid] = JsonCodecMaker.make[String].asInstanceOf
}
