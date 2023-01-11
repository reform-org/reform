package webapp

import kofre.base.*
import kofre.datatypes.*
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.serializer.jsoniterScala.given
import webapp.Codecs.*
import webapp.webrtc.DeltaFor

case class User(
    _username: Option[TimedVal[String]],
    _role: Option[TimedVal[String]],
    _comment: Option[TimedVal[Option[String]]],
) derives DecomposeLattice,
      Bottom {

  def withUsername(username: String) = {
    val diffSetUsername = User.empty.copy(_username = Some(TimedVal(username, myReplicaID)))

    this.merge(diffSetUsername)
  }

  def withRole(role: String) = {
    val diffSetRole = User.empty.copy(_role = Some(TimedVal(role, myReplicaID)))

    this.merge(diffSetRole)
  }

  def withComment(comment: Option[String]) = {
    val diffSetComment = User.empty.copy(_comment = Some(TimedVal(comment, myReplicaID)))

    this.merge(diffSetComment)
  }

  def username = {
    _username.map(_.value).getOrElse("not initialized")
  }

  def role = {
    _role.map(_.value).getOrElse("not initialized")
  }

  def comment = {
    _comment.map(_.value.getOrElse("no account")).getOrElse("not initialized")
  }
}

object User {
  val empty: User = User(None, None, None)

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make

  implicit val deltaCodec: JsonValueCodec[DeltaFor[User]] = JsonCodecMaker.make
}
