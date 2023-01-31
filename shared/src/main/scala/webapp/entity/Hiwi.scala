package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*
import webapp.entity.Attribute.given
import webapp.webrtc.DeltaFor

case class Hiwi(
    firstName: Attribute[String] = Attribute.empty,
    lastName: Attribute[String] = Attribute.empty,
    hours: Attribute[Int] = Attribute.empty,
    eMail: Attribute[String] = Attribute.empty,
    birthdate: Attribute[Long] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Hiwi]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default =
    Hiwi(Attribute.empty, Attribute.empty, Attribute.empty, Attribute.empty, Attribute.empty, Attribute.default)

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = firstName

  def withExists(exists: Boolean): Hiwi = {
    this.copy(_exists = _exists.set(exists))
  }

}

object Hiwi {
  val empty: Hiwi = Hiwi()

  implicit val codec: JsonValueCodec[Hiwi] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Hiwi]] = JsonCodecMaker.make
}
