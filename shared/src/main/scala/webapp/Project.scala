package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.base.*
import kofre.syntax.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import webapp.Codecs.*
import webapp.webrtc.DeltaFor

given [A]: Bottom[Option[TimedVal[A]]] = new Bottom[Option[TimedVal[A]]] {
  def empty = None
}

case class Project(
    _name: Option[TimedVal[String]],
    _maxHours: Option[TimedVal[Int]],
    _accountName: Option[TimedVal[Option[String]]],
    _exists: Option[TimedVal[Boolean]],
) derives DecomposeLattice,
      Bottom {

  def withName(name: String) = {
    val diffSetName = Project.empty.copy(_name = Some(LastWriterWins.now(name, myReplicaID)))

    this.merge(diffSetName)
  }

  def withAccountName(accountName: Option[String]) = {
    val diffSetAccountName = Project.empty.copy(_accountName = Some(LastWriterWins.now(accountName, myReplicaID)))

    this.merge(diffSetAccountName)
  }

  def withMaxHours(maxHours: Int) = {
    val diffSetMaxHours = Project.empty.copy(_maxHours = Some(LastWriterWins.now(maxHours, myReplicaID)))

    this.merge(diffSetMaxHours)
  }

  def withExists(exists: Boolean) = {
    val diffSetExists = Project.empty.copy(_exists = Some(LastWriterWins.now(exists, myReplicaID)))

    this.merge(diffSetExists)
  }

  def name = {
    _name.map(_.payload).getOrElse("not initialized")
  }

  def maxHours = {
    _maxHours.map(_.payload).getOrElse(0)
  }

  def accountName = {
    _accountName.map(_.payload.getOrElse("no account")).getOrElse("not initialized")
  }

  def exists = {
    _exists.map(_.payload).getOrElse(true)
  }
}

object Project {
  val empty: Project = Project(None, None, None, None)

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Project]] = JsonCodecMaker.make
}
