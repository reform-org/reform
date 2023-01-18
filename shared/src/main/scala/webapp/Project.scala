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
    _name: MultiValueRegister[String],
    _maxHours: MultiValueRegister[Int],
    _accountName: MultiValueRegister[Option[String]],
    _exists: MultiValueRegister[Boolean],
) derives DecomposeLattice,
      Bottom {

  def withName(name: String) = {
    val diffSetName = Project.empty.copy(_name = _name.write(myReplicaID, name))

    this.merge(diffSetName)
  }

  def withAccountName(accountName: Option[String]) = {
    val diffSetAccountName = Project.empty.copy(_accountName = _accountName.write(myReplicaID, accountName))

    this.merge(diffSetAccountName)
  }

  def withMaxHours(maxHours: Int) = {
    val diffSetMaxHours = Project.empty.copy(_maxHours = _maxHours.write(myReplicaID, maxHours))

    this.merge(diffSetMaxHours)
  }

  def withExists(exists: Boolean) = {
    val diffSetExists = Project.empty.copy(_exists = _exists.write(myReplicaID, exists))

    this.merge(diffSetExists)
  }

  def name = {
    _name.values.headOption.getOrElse("not initialized")
  }

  def maxHours = {
    _maxHours.values.headOption.getOrElse(0)
  }

  def accountName: Iterator[(VectorClock, Option[String])] = {
    _accountName.versions.iterator
  }

  def exists = {
    _exists.values.headOption.getOrElse(true)
  }
}

object Project {
  val empty: Project = Project(
    MultiValueRegister(Map.empty),
    MultiValueRegister(Map.empty),
    MultiValueRegister(Map.empty),
    MultiValueRegister(Map.empty),
  )

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Project]] = JsonCodecMaker.make
}
