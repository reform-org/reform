package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import kofre.base.*
import rescala.default.*
import webapp.Codecs.*
import webapp.webrtc.DeltaFor
import webapp.entity.Attribute.given

case class User(
    var _username: Attribute[String] = Attribute.empty,
    var _role: Attribute[String] = Attribute.empty,
    var _comment: Attribute[Option[String]] = Attribute.empty,
    var _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[User]
    derives DecomposeLattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default = User(Attribute.empty, Attribute.empty, Attribute.default, Attribute.default)

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _username

  def withExists(exists: Boolean): User = {
    this.copy(_exists = _exists.set(exists))
  }
}

object User {
  val empty: User = User()

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[User]] = JsonCodecMaker.make
}
