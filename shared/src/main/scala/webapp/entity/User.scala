package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.base.*
import webapp.Codecs.*
import webapp.entity.Attribute.given

case class User(
    username: Attribute[String] = Attribute.empty,
    role: Attribute[String] = Attribute.empty,
    comment: Attribute[Option[String]] = Attribute.empty,
    exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[User]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: User = User(Attribute.empty, Attribute.empty, Attribute.default, Attribute(true))

  def identifier: Attribute[String] = username

  def withExists(_exists: Boolean): User = {
    this.copy(exists = exists.set(_exists))
  }
}

object User {
  val empty: User = User()

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
