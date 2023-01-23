package webapp.entity

import kofre.base.*
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.time.VectorClock
import webapp.Codecs.myReplicaID

case class Attribute[T](register: MultiValueRegister[T]) {

  def getAll: List[T] = {
    register.versions.toList.sortBy(_._1)(using VectorClock.vectorClockTotalOrdering).map(_._2)
  }

  def get: Option[T] = {
    getAll.headOption
  }

  def set(newValue: T): Attribute[T] = {
    this.copy(register = register.write(myReplicaID, newValue))
  }
}

object Attribute {

  def empty[T]: Attribute[T] = Attribute(MultiValueRegister(Map.empty))

  given bottom[T]: Bottom[Attribute[T]] = Bottom.derived

  given decomposeLattice[T]: DecomposeLattice[Attribute[T]] = DecomposeLattice.derived
}
