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
    var _username: Attribute[String] = Attribute.empty.set(""),
    var _role: Attribute[String] = Attribute.empty.set(""),
    var _comment: Attribute[Option[String]] = Attribute.empty.set(None),
    var _exists: Attribute[Boolean] = Attribute.empty.set(true),
) extends Entity[User]
    derives DecomposeLattice,
      Bottom {

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
