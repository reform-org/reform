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

case class SalaryChange(
    var _value: Attribute[Int] = Attribute.empty.set(0),
    var _paymentLevel: Attribute[String] = Attribute.empty.set(""),
    var _fromDate: Attribute[Long] = Attribute.empty.set(0),
    var _exists: Attribute[Boolean] = Attribute.empty.set(true),
) extends Entity[SalaryChange]
    derives DecomposeLattice,
      Bottom {

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
