package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*

case class Hiwi(
    firstName: Attribute[String] = Attribute.empty,
    lastName: Attribute[String] = Attribute.empty,
    gender: Attribute[String] = Attribute.empty,
    eMail: Attribute[String] = Attribute.empty,
    birthdate: Attribute[Long] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Hiwi]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: Hiwi =
    Hiwi(
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute(true),
    )

  def identifier: Attribute[String] = eMail

  def withExists(_exists: Boolean): Hiwi = {
    this.copy(exists = exists.set(_exists))
  }

}

object Hiwi {
  val empty: Hiwi = Hiwi()

  implicit val codec: JsonValueCodec[Hiwi] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
