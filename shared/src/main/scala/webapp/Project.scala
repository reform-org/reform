package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.base.*
import kofre.syntax.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import webapp.Codecs.*
import webapp.webrtc.DeltaFor
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.time.VectorClock
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig

case class Project(
    _name: Attribute[String],
    _maxHours: Attribute[Int],
    _accountName: Attribute[Option[String]],
    _exists: Attribute[Boolean],
) extends webapp.pages.Entity[Project]
    derives DecomposeLattice,
      Bottom {

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _name

  def withExists(exists: Boolean): Project = {
    this.copy(_exists = _exists.set(exists))
  }

  def getUIAttributes: List[UIAttribute[Project, ? <: Any]] = {
    List(
      UIAttribute(
        _name,
        va => va.toString(),
        u => u,
        (u, x) => {
          u.copy(_name = _name.set(x))
        },
      ),
      UIAttribute(
        _maxHours,
        va => va.toString(),
        u => u.toInt,
        (u, x) => {
          u.copy(_maxHours = _maxHours.set(x))
        },
      ),
      UIAttribute(
        _accountName,
        va => va.getOrElse("no account name").toString(),
        u => Some(u),
        (u, x) => {
          u.copy(_accountName = _accountName.set(x))
        },
      ),
    )
  }
}

object Project {
  val empty: Project = Project(
    Attribute(MultiValueRegister(Map.empty)),
    Attribute(MultiValueRegister(Map.empty)),
    Attribute(MultiValueRegister(Map.empty)),
    Attribute(MultiValueRegister(Map.empty)),
  )

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Project]] = JsonCodecMaker.make
}
