package webapp

import kofre.base.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.serializer.jsoniterScala.given
import webapp.Codecs.*
import webapp.webrtc.DeltaFor
import kofre.datatypes.alternatives.MultiValueRegister
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import kofre.time.VectorClock

case class User(
    _username: MultiValueRegister[String],
    _role: MultiValueRegister[String],
    _comment: MultiValueRegister[Option[String]],
    _exists: MultiValueRegister[Boolean],
) derives DecomposeLattice,
      Bottom {

  def withUsername(username: String) = {
    val diffSetUsername = User.empty.copy(_username = _username.write(myReplicaID, username))

    this.merge(diffSetUsername)
  }

  def withRole(role: String) = {
    val diffSetRole = User.empty.copy(_role = _role.write(myReplicaID, role))

    this.merge(diffSetRole)
  }

  def withComment(comment: Option[String]) = {
    val diffSetComment = User.empty.copy(_comment = _comment.write(myReplicaID, comment))

    this.merge(diffSetComment)
  }

  def withExists(exists: Boolean) = {
    val diffSetExists = User.empty.copy(_exists = _exists.write(myReplicaID, exists))

    this.merge(diffSetExists)
  }

  def username = {
    _username.versions.toList.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }

  def role = {
    _role.versions.toList.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }

  def comment = {
    _comment.versions.toList
      .sortBy(_._1)(using VectorClock.vectorClockTotalOrdering)
      .map(_._2)
      .map(_.getOrElse("no comment"))
  }

  def exists = {
    _exists.versions.toList.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }
}

object User {
  val empty: User = User(
    MultiValueRegister(Map.empty),
    MultiValueRegister(Map.empty),
    MultiValueRegister(Map.empty),
    MultiValueRegister(Map.empty),
  )

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[User]] = JsonCodecMaker.make
}
