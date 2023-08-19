package de.tu_darmstadt.informatik.st.reform.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*

case class ContractSchema(
    name: Attribute[String] = Attribute.empty,
    files: Attribute[Seq[String]] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[ContractSchema]
    derives Lattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: ContractSchema = ContractSchema(Attribute.empty, Attribute(Seq()), Attribute(true))

  def identifier: Attribute[String] = name

  def withExists(exists: Boolean): ContractSchema = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.get.getOrElse(true)

}

object ContractSchema {
  val empty: ContractSchema = ContractSchema()

  implicit val codec: JsonValueCodec[ContractSchema] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
