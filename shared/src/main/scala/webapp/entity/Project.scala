package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*
import webapp.webrtc.DeltaFor
import webapp.entity.Attribute.given

case class Project(
    _name: Attribute[String] = Attribute.empty,
    _maxHours: Attribute[Int] = Attribute.empty,
    _accountName: Attribute[Option[String]] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Project]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = Project(Attribute.empty, Attribute.empty, Attribute.default, Attribute.default)

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _name

  def withExists(exists: Boolean): Project = {
    this.copy(_exists = _exists.set(exists))
  }

}

object Project {
  val empty: Project = Project()

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Project]] = JsonCodecMaker.make
}
