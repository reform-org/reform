package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*
import webapp.entity.Attribute.given
import webapp.webrtc.DeltaFor

case class ContractSchema(
    name: Attribute[String] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[ContractSchema]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = ContractSchema(Attribute.empty, Attribute.default)

  def identifier: Attribute[String] = name

  def withExists(_exists: Boolean): ContractSchema = {
    this.copy(exists = exists.set(_exists))
  }

}

object ContractSchema {
  val empty: ContractSchema = ContractSchema()

  implicit val codec: JsonValueCodec[ContractSchema] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
