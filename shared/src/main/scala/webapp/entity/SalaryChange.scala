package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import kofre.base.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.time.VectorClock
import loci.serializer.jsoniterScala.given
import rescala.default.*
import webapp.Codecs.*
import webapp.webrtc.DeltaFor
import webapp.entity.Attribute.given

case class SalaryChange(
    var _value: Attribute[Int] = Attribute.empty,
    var _paymentLevel: Attribute[String] = Attribute.empty,
    var _fromDate: Attribute[Long] = Attribute.empty,
    var _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[SalaryChange]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = SalaryChange(Attribute.empty, Attribute.empty, Attribute.empty, Attribute.default)

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _paymentLevel

  def withExists(exists: Boolean): SalaryChange = {
    this.copy(_exists = _exists.set(exists))
  }
}

object SalaryChange {
  val empty: SalaryChange = SalaryChange()

  implicit val codec: JsonValueCodec[SalaryChange] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[SalaryChange]] = JsonCodecMaker.make
}
