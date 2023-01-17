package webapp

import kofre.base.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.serializer.jsoniterScala.given
import webapp.Codecs.*
import webapp.webrtc.DeltaFor

case class User(
    _username: Option[TimedVal[String]],
    _role: Option[TimedVal[String]],
    _comment: Option[TimedVal[Option[String]]],
    _exists: Option[TimedVal[Boolean]],
) derives DecomposeLattice,
      Bottom {

  def withUsername(username: String) = {
    val diffSetUsername = User.empty.copy(_username = Some(LastWriterWins.now(username, myReplicaID)))

    this.merge(diffSetUsername)
  }

  def withRole(role: String) = {
    val diffSetRole = User.empty.copy(_role = Some(LastWriterWins.now(role, myReplicaID)))

    this.merge(diffSetRole)
  }

  def withComment(comment: Option[String]) = {
    val diffSetComment = User.empty.copy(_comment = Some(LastWriterWins.now(comment, myReplicaID)))

    this.merge(diffSetComment)
  }

  def withExists(exists: Boolean) = {
    val diffSetExists = User.empty.copy(_exists = Some(LastWriterWins.now(exists, myReplicaID)))

    this.merge(diffSetExists)
  }

  def username = {
    _username.map(_.payload).getOrElse("not initialized")
  }

  def role = {
    _role.map(_.payload).getOrElse("not initialized")
  }

  def comment = {
    _comment.map(_.payload.getOrElse("no account")).getOrElse("not initialized")
  }

  def exists = {
    _exists.map(_.payload).getOrElse(true)
  }
}

object User {
  val empty: User = User(None, None, None, None)

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make

  implicit val deltaCodec: JsonValueCodec[DeltaFor[User]] = JsonCodecMaker.make
}
