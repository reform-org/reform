package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*
import webapp.webrtc.DeltaFor
import webapp.entity.Attribute.given

case class ContractSchema(
    _name: Attribute[String] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[ContractSchema]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = ContractSchema(Attribute.empty, Attribute.default)

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _name

  def withExists(exists: Boolean): ContractSchema = {
    this.copy(_exists = _exists.set(exists))
  }

}

object ContractSchema {
  val empty: ContractSchema = ContractSchema()

  implicit val codec: JsonValueCodec[ContractSchema] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[ContractSchema]] = JsonCodecMaker.make
}
