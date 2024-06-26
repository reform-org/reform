package de.tu_darmstadt.informatik.st.reform.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*
import de.tu_darmstadt.informatik.st.reform.entity.Attribute.given
import kofre.base.*

case class Project(
    name: Attribute[String] = Attribute.empty,
    maxHours: Attribute[Int] = Attribute.empty,
    accountName: Attribute[Option[String]] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Project]
    derives Lattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: Project = Project()

  def identifier: Attribute[String] = Attribute(
    s"${name.getOrElse("")} - ${accountName.option.flatten.getOrElse("")}",
  )

  def withExists(exists: Boolean): Project = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.getOrElse(true)
}

object Project {
  val empty: Project = Project()
  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
