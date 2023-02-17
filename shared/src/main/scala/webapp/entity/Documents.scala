package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*
import webapp.entity.Attribute.given

case class Document(
    name: Attribute[String] = Attribute.empty,
    fileName: Attribute[String] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Document]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: Document =
    Document(Attribute.default, Attribute.default, Attribute(true))

  def identifier: Attribute[String] = name

  def withExists(_exists: Boolean): Document = {
    this.copy(exists = exists.set(_exists))
  }

}

object Document {
  val empty: Document = Document()

  implicit val codec: JsonValueCodec[Document] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
