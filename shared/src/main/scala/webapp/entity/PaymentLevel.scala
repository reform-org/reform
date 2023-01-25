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

case class PaymentLevel(
    _title: Attribute[String] = Attribute.empty.set(""),
    _exists: Attribute[Boolean] = Attribute.empty.set(true),
) extends Entity[PaymentLevel]
    derives DecomposeLattice,
      Bottom {

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
