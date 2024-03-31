package de.tu_darmstadt.informatik.st.reform.entity

import de.tu_darmstadt.informatik.st.reform.BasicCodecs.myReplicaID
import kofre.base.*
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.time.VectorClock

case class Attribute[T](register: MultiValueRegister[T]) {

  def getAll: Seq[T] = {
    register.versions.toSeq.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }

  def option: Option[T] = getAll.headOption

  def get: T = getAll.head

  def getOrElse[U >: T](default: => U): U =
    getAll.headOption.getOrElse(default)

  def hasValue: Boolean = register.versions.nonEmpty

  def set(newValue: T): Attribute[T] = {
    this.copy(register = register.write(myReplicaID, newValue))
  }

  def update(f: T => T): Attribute[T] =
    option match {
      case Some(x) => set(f(x))
      case None    => this
    }
}

object Attribute {

  def apply[T](value: T): Attribute[T] = Attribute.empty.set(value)

  def empty[T]: Attribute[T] = Attribute(MultiValueRegister(Map.empty))

  given bottom[T]: Bottom[Attribute[T]] = Bottom.derived

  given decomposeLattice[T]: Lattice[Attribute[T]] = Lattice.derived
}
