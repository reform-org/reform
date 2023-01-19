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
import rescala.default.*
import outwatch.*
import outwatch.dsl.*
import outwatch.StaticVModifier

case class UIAttribute[EntityType, AttributeType](attribute: Attribute[AttributeType], readConverter: AttributeType => String, writeConverter: String => AttributeType, setter: (EntityType, AttributeType) => EntityType) {
  
  def update[T](setter: EntityType => EntityType, editingValue: Var[Option[EntityType]], x: String) = {
    editingValue.transform(value => {
      value.map(p => setter(p))
    })
  }

  def render(editingValue: Var[Option[EntityType]]) = {
    td(
      input(
        value := attribute.getAll().map(x => readConverter(x)).mkString("/"),
        onInput.value --> {
          val evt = Evt[String]()
          evt.observe(x => update(l => setter(l, writeConverter(x)), editingValue, x))
          evt
        },
        VModifier.prop("placeholder") := "TODO",
      ),
    )
  }
}

case class Attribute[T](register: MultiValueRegister[T]) {

  def getAll(): List[T] = {
    register.versions.toList.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }

  def get(): Option[T] = {
    getAll().headOption
  }

  def set(newValue: T): Attribute[T] = {
    this.copy(register = register.write(myReplicaID, newValue))
  }
}

object Attribute {
  given bottom[T]: Bottom[Attribute[T]] = Bottom.derived

  given decomposeLattice[T]: DecomposeLattice[Attribute[T]] = DecomposeLattice.derived
}

case class User(
    _username: Attribute[String],
    _role: Attribute[String],
    _comment: Attribute[Option[String]],
    _exists: Attribute[Boolean],
) extends webapp.pages.Entity[User] derives DecomposeLattice,
      Bottom {

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _username

  def withExists(exists: Boolean): User = {
    this.copy(_exists = _exists.set(exists))
  }

  def getUIAttributes: List[UIAttribute[User, ? <: Any]] = {
    List(
      UIAttribute(
        _username,
                va => va.toString(),
                u => u,
                (u, x) => {
                  u.copy(_username = _username.set(x))
                },
              ),
              UIAttribute(
                _role,
                va => va.toString(),
                u => u,
                (u, x) => {
                  u.copy(_role = _role.set(x))
                },
              ),
              UIAttribute(
                _comment,
                va => va.getOrElse("no comment").toString(),
                u => Some(u),
                (u, x) => {
                  u.copy(_comment = _comment.set(x))
                },
              ),
    )
  }
}

object User {
  val empty: User = User(
    Attribute(MultiValueRegister(Map.empty)),
    Attribute(MultiValueRegister(Map.empty)),
    Attribute(MultiValueRegister(Map.empty)),
    Attribute(MultiValueRegister(Map.empty)),
  )

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[User]] = JsonCodecMaker.make
}
