package webapp.utils

import rescala.default.*

object Seqnal {

  implicit class SeqOfSignalOps[T](self: Seq[Signal[T]]) {

    def seqToSignal: Signal[Seq[T]] = Signal(self).flatten
  }

  implicit class SigOfSeq[T](self: Signal[Seq[T]]) {

    def mapInside[U](f: T => U): Signal[Seq[U]] = self.map(_.map(f))
  }

  implicit class SeqOps[T](self: Seq[T]) {

    def filterSignal(p: T => Signal[Boolean]): Signal[Seq[T]] = self
      .map(e =>
        p(e).map(if _ then Seq(e) else Seq.empty)
      )
      .seqToSignal
      .map(_.flatten)

    def mapToSignal[U](f: T => Signal[U]): Signal[Seq[U]] = self.map(f).seqToSignal
  }

  implicit class OptionOfSignalOps[T](self: Option[Signal[T]]) {

    def optionToSignal: Signal[Option[T]] = Signal(self).flatten
  }

  implicit class OptionOps[T](self: Option[T]) {

    def mapToSignal[U](f: T => Signal[U]): Signal[Option[U]] = self.map(f).optionToSignal
  }
}