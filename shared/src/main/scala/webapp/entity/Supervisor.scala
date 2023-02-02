package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*
import webapp.entity.Attribute.given
import webapp.webrtc.DeltaFor

case class Supervisor(
    firstName: Attribute[String] = Attribute.empty,
    lastName: Attribute[String] = Attribute.empty,
    eMail: Attribute[String] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Supervisor]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = Supervisor(Attribute.empty, Attribute.empty, Attribute.empty, Attribute.default)

  def identifier: Attribute[String] = firstName

  def withExists(_exists: Boolean): Supervisor = {
    this.copy(exists = exists.set(_exists))
  }

}

object Supervisor {
  val empty: Supervisor = Supervisor()

  implicit val codec: JsonValueCodec[Supervisor] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Supervisor]] = JsonCodecMaker.make
}
