package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.syntax.*
import kofre.time.VectorClock
import webapp.Codecs.*
import webapp.webrtc.DeltaFor

case class Contract(
    contractAssociatedHiwi: Attribute[String] = Attribute.empty,
    contractAssociatedPaymentLevel: Attribute[String] = Attribute.empty,
    contractAssociatedSupervisor: Attribute[String] = Attribute.empty,
    contractStartDate: Attribute[Long] = Attribute.empty,
    contractEndDate: Attribute[Long] = Attribute.empty,
    contractType: Attribute[String] = Attribute.empty,
    contractHoursPerMonth: Attribute[Int] = Attribute.empty,
    isDraft: Attribute[Boolean] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Contract]
    derives DecomposeLattice,
      Bottom {

  def identifier: Attribute[String] = contractAssociatedHiwi

  def withExists(_exists: Boolean): Contract = {
    this.copy(exists = exists.set(_exists))
  }

}

object Contract {
  val empty: Contract = Contract()

  implicit val codec: JsonValueCodec[Contract] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Contract]] = JsonCodecMaker.make
}
