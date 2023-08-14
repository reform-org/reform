package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import webapp.BasicCodecs.*
import webapp.entity.Attribute.given

case class Project(
    name: Attribute[String] = Attribute.empty,
    maxHours: Attribute[Int] = Attribute.empty,
    accountName: Attribute[Option[String]] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Project]
    derives Lattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: Project = Project(Attribute.empty, Attribute.empty, Attribute.default, Attribute(true))

  def identifier: Attribute[String] = Attribute(
    s"${name.get.getOrElse("")} - ${accountName.get.getOrElse(None).getOrElse("")}",
  )

  def withExists(exists: Boolean): Project = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.get.getOrElse(true)
}

object Project {
  val empty: Project = Project()
  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}