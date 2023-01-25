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
    name: Attribute[String] = Attribute.empty,
    maxHours: Attribute[Int] = Attribute.empty,
    accountName: Attribute[Option[String]] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Project]
    derives DecomposeLattice,
      Bottom {

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = name

  def withExists(exists: Boolean): Project = {
    this.copy(_exists = _exists.set(exists))
  }

}

object Project {
  val empty: Project = Project()

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Project]] = JsonCodecMaker.make
}
