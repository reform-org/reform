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

case class Attribute[T](value: MultiValueRegister[T]) {

  def getAll(): List[T] = {
    value.versions.toList.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }

  def get(): Option[T] = {
    getAll().headOption
  }

  def set(newValue: T): Attribute[T] = {
    this.copy(value = value.write(myReplicaID, newValue))
  }
}

object Attribute {
  given bottom[T]: Bottom[Attribute[T]] with { override def empty: Attribute[T] = Attribute(MultiValueRegister(Map.empty)) }

  given decomposeLattice[T]: DecomposeLattice[Attribute[T]] = DecomposeLattice.derived
}

case class User(
    _username: Attribute[String],
    _role: Attribute[String],
    _comment: Attribute[Option[String]],
    _exists: Attribute[Boolean]
) derives DecomposeLattice, Bottom {

  def withUsername(username: String) = {
    val diffSetUsername = User.empty.copy(_username = _username.set(username))

   0// this.merge(diffSetUsername)
  }

  def withRole(role: String) = {
    val diffSetRole = User.empty.copy(_role = _role.set(role))

    //this.merge(diffSetRole)
  }

  def withComment(comment: Option[String]) = {
    val diffSetComment = User.empty.copy(_comment = _comment.set(comment))

    //this.merge(diffSetComment)
  }

  def withExists(exists: Boolean) = {
    val diffSetExists = User.empty.copy(_exists = _exists.set(exists))

    //this.merge(diffSetExists)
  }
}

object User {
  val empty: User = User(
    Attribute(MultiValueRegister(Map.empty)),
   Attribute(MultiValueRegister(Map.empty)),
   Attribute(MultiValueRegister(Map.empty)),
   Attribute(MultiValueRegister(Map.empty))
  )

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[User]] = JsonCodecMaker.make
}
