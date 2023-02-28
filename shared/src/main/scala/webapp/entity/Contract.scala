package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*
import webapp.webrtc.DeltaFor

case class Contract(
    contractAssociatedProject: Attribute[String] = Attribute.empty,
    contractAssociatedHiwi: Attribute[String] = Attribute.empty,
    contractAssociatedPaymentLevel: Attribute[String] = Attribute.empty,
    contractAssociatedSupervisor: Attribute[String] = Attribute.empty,
    contractStartDate: Attribute[Long] = Attribute.empty,
    contractEndDate: Attribute[Long] = Attribute.empty,
    contractType: Attribute[String] = Attribute.empty,
    contractHoursPerMonth: Attribute[Int] = Attribute.empty,
    isDraft: Attribute[Boolean] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Contract]
    derives DecomposeLattice,
      Bottom {

  def identifier: Attribute[String] = contractAssociatedHiwi

  def withExists(exists: Boolean): Contract = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.get.getOrElse(true)

  def default: Contract =
    Contract(
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute(true),
      Attribute(true),
    )

}

object Contract {
  val empty: Contract = Contract()

  implicit val codec: JsonValueCodec[Contract] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Contract]] = JsonCodecMaker.make
}
