package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*
import webapp.webrtc.DeltaFor
import webapp.entity.Attribute.given

case class PaymentLevel(
    _title: Attribute[String] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[PaymentLevel]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = PaymentLevel(Attribute.empty, Attribute.default)

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _title

  def withExists(exists: Boolean): PaymentLevel = {
    this.copy(_exists = _exists.set(exists))
  }

}

object PaymentLevel {
  val empty: PaymentLevel = PaymentLevel()

  implicit val codec: JsonValueCodec[PaymentLevel] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[PaymentLevel]] = JsonCodecMaker.make
}
