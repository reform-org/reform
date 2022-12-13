package webapp

import kofre.base.Lattice
import kofre.base.DecomposeLattice
import kofre.base.Bottom
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

case class GrowOnlySet[A](set: Set[A])

object GrowOnlySet {
  given stringCodec: JsonValueCodec[GrowOnlySet[String]] = JsonCodecMaker.make

  given bottom[E]: Bottom[GrowOnlySet[E]] = new {
    override def empty: GrowOnlySet[E] = GrowOnlySet(Set.empty)
  }

  implicit def GrowOnlySetLattice[A]: DecomposeLattice[GrowOnlySet[A]] = new DecomposeLattice[GrowOnlySet[A]] {
    def merge(left: GrowOnlySet[A], right: GrowOnlySet[A]): GrowOnlySet[A] = GrowOnlySet(left.set.union(right.set))

    def decompose(a: GrowOnlySet[A]): Iterable[GrowOnlySet[A]] = List(a)
  }
}
