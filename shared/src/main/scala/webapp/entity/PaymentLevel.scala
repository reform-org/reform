package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*

case class PaymentLevel(
    title: Attribute[String] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[PaymentLevel]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: PaymentLevel = PaymentLevel(Attribute.empty, Attribute(true))

  def identifier: Attribute[String] = title

  def withExists(_exists: Boolean): PaymentLevel = {
    this.copy(exists = exists.set(_exists))
  }

}

object PaymentLevel {
  val empty: PaymentLevel = PaymentLevel()

  implicit val codec: JsonValueCodec[PaymentLevel] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
