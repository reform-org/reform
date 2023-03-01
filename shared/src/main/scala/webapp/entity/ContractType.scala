package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*

case class ContractType(
    name: Attribute[String] = Attribute.empty,
    files: Attribute[Seq[String]] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[ContractType]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: ContractType = ContractType(Attribute.empty, Attribute(Seq()), Attribute(true))

  def identifier: Attribute[String] = name

  def withExists(exists: Boolean): ContractType = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.get.getOrElse(true)

}

object ContractType {
  val empty: ContractType = ContractType()

  implicit val codec: JsonValueCodec[ContractType] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
