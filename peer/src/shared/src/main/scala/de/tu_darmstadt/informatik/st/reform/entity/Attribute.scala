package de.tu_darmstadt.informatik.st.reform.entity

import de.tu_darmstadt.informatik.st.reform.BasicCodecs.myReplicaID
import kofre.base.*
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.time.VectorClock

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

  def apply[T](value: T): Attribute[T] = Attribute.empty.set(value)

  given stringDefault: Default[String] with {
    def default = ""
  }

  given defaultInt: Default[Int] with {
    def default: Int = 0
  }

  given defaultLong: Default[Long] with {
    def default: Long = 0
  }

  given defaultOptional[T]: Default[Option[T]] with {
    def default: Option[T] = None
  }

  given attributeDefault[T](using defaultT: Default[T]): Default[Attribute[T]] with {
    def default: Attribute[T] = Attribute(defaultT.default)
  }

  def default[T](using defaultAttribute: Default[T]): Attribute[T] = attributeDefault.default

  def empty[T]: Attribute[T] = Attribute(MultiValueRegister(Map.empty))

  given bottom[T]: Bottom[Attribute[T]] = Bottom.derived

  given decomposeLattice[T]: Lattice[Attribute[T]] = Lattice.derived
}
