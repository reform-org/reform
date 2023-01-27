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
    _contractAssociatedHiwi: Attribute[String] = Attribute.empty,
    _contractAssociatedPaymentLevel: Attribute[String] = Attribute.empty,
    _contractAssociatedSupervisor: Attribute[String] = Attribute.empty,
    _contractStartDate: Attribute[Long] = Attribute.empty,
    _contractEndDate: Attribute[Long] = Attribute.empty,
    _contractType: Attribute[String] = Attribute.empty,
    _contractHoursPerMonth: Attribute[Int] = Attribute.empty,
    _isDraft: Attribute[Boolean] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,

) extends Entity[Contract]
    derives DecomposeLattice,
      Bottom {

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _contractAssociatedHiwi

  def withExists(exists: Boolean): Contract = {
    this.copy(_exists = _exists.set(exists))
  }

}

object Contract {
  val empty: Contract = Contract()

  implicit val codec: JsonValueCodec[Contract] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Contract]] = JsonCodecMaker.make
}
