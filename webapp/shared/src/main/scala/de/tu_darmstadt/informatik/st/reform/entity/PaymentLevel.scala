package de.tu_darmstadt.informatik.st.reform.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*
import kofre.base.*

case class PaymentLevel(
    title: Attribute[String] = Attribute.empty,
    pdfCheckboxName: Attribute[String] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[PaymentLevel]
    derives Lattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: PaymentLevel = PaymentLevel(Attribute.empty, Attribute.empty, Attribute(true))

  def identifier: Attribute[String] = title

  def withExists(exists: Boolean): PaymentLevel = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.get.getOrElse(true)
}

object PaymentLevel {
  val empty: PaymentLevel = PaymentLevel()

  implicit val codec: JsonValueCodec[PaymentLevel] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
