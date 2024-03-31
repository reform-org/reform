package de.tu_darmstadt.informatik.st.reform.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*
import kofre.base.*

case class Hiwi(
    firstName: Attribute[String] = Attribute.empty,
    lastName: Attribute[String] = Attribute.empty,
    eMail: Attribute[String] = Attribute.empty,
    birthdate: Attribute[Long] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Hiwi]
    derives Lattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: Hiwi =
    Hiwi(
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute.empty,
      Attribute(true),
    )

  def identifier: Attribute[String] = Attribute(s"${firstName.getOrElse("")} ${lastName.getOrElse("")}")

  def withExists(exists: Boolean): Hiwi = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.getOrElse(true)
}

object Hiwi {
  val empty: Hiwi = Hiwi()

  implicit val codec: JsonValueCodec[Hiwi] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
