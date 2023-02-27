package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*

case class PaymentLevel(
    title: Attribute[String] = Attribute.empty,
    pdfCheckboxName: Attribute[String] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[PaymentLevel]
    derives DecomposeLattice,
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
