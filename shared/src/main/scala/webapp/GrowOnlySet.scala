package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import kofre.base.*

case class GrowOnlySet[A](set: Set[A]) {

  def add(value: A): GrowOnlySet[A] =
    GrowOnlySet(set + value)

  def union(other: GrowOnlySet[A]): GrowOnlySet[A] =
    GrowOnlySet(set.union(other.set))
}

object GrowOnlySet {

  def empty[A]: GrowOnlySet[A] = GrowOnlySet(Set.empty)

  given stringCodec: JsonValueCodec[GrowOnlySet[String]] = JsonCodecMaker.make

  given bottom[E]: Bottom[GrowOnlySet[E]] = new {
    override def empty: GrowOnlySet[E] = GrowOnlySet.empty
  }

  implicit def GrowOnlySetLattice[A]: DecomposeLattice[GrowOnlySet[A]] = new DecomposeLattice[GrowOnlySet[A]] {
    def merge(left: GrowOnlySet[A], right: GrowOnlySet[A]): GrowOnlySet[A] = left.union(right)

    override def decompose(a: GrowOnlySet[A]): Iterable[GrowOnlySet[A]] = List(a)
  }
}
