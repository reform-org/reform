package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import kofre.base.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.time.VectorClock
import loci.serializer.jsoniterScala.given
import rescala.default.*
import webapp.Codecs.*
import webapp.webrtc.DeltaFor

case class User(
    var username: Attribute[String] = Attribute.empty,
    var _role: Attribute[String] = Attribute.empty,
    var comment: Attribute[Option[String]] = Attribute.empty,
    var _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[User]
    derives DecomposeLattice,
      Bottom {

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = username

  def withExists(exists: Boolean): User = {
    this.copy(_exists = _exists.set(exists))
  }
}

object User {
  val empty: User = User()

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[User]] = JsonCodecMaker.make
}
