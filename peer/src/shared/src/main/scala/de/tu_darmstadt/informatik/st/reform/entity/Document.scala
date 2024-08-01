package de.tu_darmstadt.informatik.st.reform.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*
import de.tu_darmstadt.informatik.st.reform.entity.Attribute.given
import kofre.base.*

case class Document(
    name: Attribute[String] = Attribute.empty,
    location: Attribute[String] = Attribute.empty,
    autofill: Attribute[Autofill] = Attribute(Autofill.NoFill),
    mailto: Attribute[DocumentsForWhom] = Attribute(DocumentsForWhom.ForNobody),
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Document]
    derives Lattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: Document = Document()

  def identifier: Attribute[String] = name

  def withExists(exists: Boolean): Document = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.getOrElse(true)
}

object Document {
  val empty: Document = Document()

  implicit val codec: JsonValueCodec[Document] = {
    import scala.language.unsafeNulls
    JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
  }
}
