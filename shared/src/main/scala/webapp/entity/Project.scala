package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.syntax.*
import kofre.time.VectorClock
import webapp.Codecs.*
import webapp.webrtc.DeltaFor

case class Project(
    _name: Attribute[String] = Attribute.empty.set(""),
    _maxHours: Attribute[Int] = Attribute.empty.set(0),
    _accountName: Attribute[Option[String]] = Attribute.empty.set(None),
    _exists: Attribute[Boolean] = Attribute.empty.set(true),
) extends Entity[Project]
    derives DecomposeLattice,
      Bottom {

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
