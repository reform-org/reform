package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.base.*
import webapp.Codecs.*
import webapp.entity.Attribute.given
import webapp.webrtc.DeltaFor

case class SalaryChange(
    value: Attribute[Int] = Attribute.empty,
    paymentLevel: Attribute[String] = Attribute.empty,
    fromDate: Attribute[Long] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[SalaryChange]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = SalaryChange(Attribute.empty, Attribute.empty, Attribute.empty, Attribute.default)

  def identifier: Attribute[String] = paymentLevel

  def withExists(_exists: Boolean): SalaryChange = {
    this.copy(exists = exists.set(_exists))
  }
}

object SalaryChange {
  val empty: SalaryChange = SalaryChange()

  implicit val codec: JsonValueCodec[SalaryChange] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[SalaryChange]] = JsonCodecMaker.make
}
