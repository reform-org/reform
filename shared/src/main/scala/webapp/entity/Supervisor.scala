package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*

case class Supervisor(
    name: Attribute[String] = Attribute.empty,
    eMail: Attribute[String] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Supervisor]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: Supervisor = Supervisor(Attribute.empty, Attribute.empty, Attribute(true))

  def identifier: Attribute[String] = name

  def withExists(exists: Boolean): Supervisor = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.get.getOrElse(true)
}

object Supervisor {
  val empty: Supervisor = Supervisor()

  implicit val codec: JsonValueCodec[Supervisor] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
