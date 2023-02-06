package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.Codecs.*
import webapp.entity.Attribute.given
import webapp.webrtc.DeltaFor

case class Project(
    name: Attribute[String] = Attribute.empty,
    maxHours: Attribute[Int] = Attribute.empty,
    accountName: Attribute[Option[String]] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Project]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = Project(Attribute.empty, Attribute.empty, Attribute.default, Attribute.default)

  def identifier: Attribute[String] = name

  def withExists(_exists: Boolean): Project = {
    this.copy(exists = exists.set(_exists))
  }

}

object Project {
  val empty: Project = Project()

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
