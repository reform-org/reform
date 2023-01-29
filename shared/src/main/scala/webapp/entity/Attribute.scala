package webapp.entity

import kofre.base.*
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.time.VectorClock
import webapp.Codecs.myReplicaID

trait Default[T] {
  def default: T
}

case class Attribute[T](register: MultiValueRegister[T]) {

  def getAll: Seq[T] = {
    register.versions.toSeq.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }

  def get: Option[T] = {
    getAll.headOption
  }

  def set(newValue: T): Attribute[T] = {
    this.copy(register = register.write(myReplicaID, newValue))
  }
}

object Attribute {

  given stringDefault: Default[String] with {
    def default = ""
  }

  given booleanDefault: Default[Boolean] with {
    def default = true // TODO FIXME this may not be a good idea
  }

  given defaultInt: Default[Int] with {
    def default = 0
  }

  given defaultLong: Default[Long] with {
    def default = 0
  }

  given defaultOptional[T]: Default[Option[T]] with {
    def default = None
  }

  given attributeDefault[T](using defaultT: Default[T]): Default[Attribute[T]] with {
    def default = {
      Attribute(MultiValueRegister(Map.empty).write(myReplicaID, defaultT.default))
    }
  }

  def default[T](using defaultAttribute: Default[T]): Attribute[T] = attributeDefault.default

  def empty[T]: Attribute[T] = Attribute(MultiValueRegister(Map.empty))

  given bottom[T]: Bottom[Attribute[T]] = Bottom.derived

  given decomposeLattice[T]: DecomposeLattice[Attribute[T]] = DecomposeLattice.derived
}
