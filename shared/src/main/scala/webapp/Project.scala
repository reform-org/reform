package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.base.*
import kofre.syntax.*
import kofre.datatypes.*
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
    val diffSetName = Project.empty.copy(_name = Some(TimedVal(name, myReplicaID)))

    this.merge(diffSetName)
  }

  def withAccountName(accountName: Option[String]) = {
    val diffSetAccountName = Project.empty.copy(_accountName = Some(TimedVal(accountName, myReplicaID)))

    this.merge(diffSetAccountName)
  }

  def withMaxHours(maxHours: Int) = {
    val diffSetMaxHours = Project.empty.copy(_maxHours = Some(TimedVal(maxHours, myReplicaID)))

    this.merge(diffSetMaxHours)
  }

  def withExists(exists: Boolean) = {
    val diffSetExists = Project.empty.copy(_exists = Some(TimedVal(exists, myReplicaID)))

    this.merge(diffSetExists)
  }

  def name = {
    _name.map(_.value).getOrElse("not initialized")
  }

  def maxHours = {
    _maxHours.map(_.value).getOrElse(0)
  }

  def accountName = {
    _accountName.map(_.value.getOrElse("no account")).getOrElse("not initialized")
  }

  def exists = {
    _exists.map(_.value).getOrElse(true)
  }
}

object Project {
  val empty: Project = Project(None, None, None, None)

  implicit val codec: JsonValueCodec[Project] = JsonCodecMaker.make

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Project]] = JsonCodecMaker.make
}
