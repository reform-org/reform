package de.tu_darmstadt.informatik.st.reform.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*
import de.tu_darmstadt.informatik.st.reform.webrtc.DeltaFor
import kofre.base.*

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
    requiredDocuments: Attribute[Seq[String]] = Attribute.empty,
    isSigned: Attribute[Boolean] = Attribute.empty,
    isSubmitted: Attribute[Boolean] = Attribute.empty,
    reminderSentDate: Attribute[Long] = Attribute.empty,
    contractSentDate: Attribute[Long] = Attribute.empty,
    letterSentDate: Attribute[Long] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Contract]
    derives Lattice,
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
      Attribute.empty,
      Attribute(false),
      Attribute(false),
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute(true),
    )

}

object Contract {
  val empty: Contract = Contract()

  implicit val codec: JsonValueCodec[Contract] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Contract]] = JsonCodecMaker.make
}
