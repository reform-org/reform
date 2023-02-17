package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*
import webapp.entity.Attribute.given

case class RequiredDocument(
    name: Attribute[String] = Attribute.empty,
    fileName: Attribute[String] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[RequiredDocument]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: RequiredDocument =
    RequiredDocument(Attribute.default, Attribute.default, Attribute(true))

  def identifier: Attribute[String] = name

  def withExists(_exists: Boolean): RequiredDocument = {
    this.copy(exists = exists.set(_exists))
  }

}

object RequiredDocument {
  val empty: RequiredDocument = RequiredDocument()

  implicit val codec: JsonValueCodec[RequiredDocument] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
