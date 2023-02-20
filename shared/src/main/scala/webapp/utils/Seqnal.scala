package webapp.utils

import rescala.core.Disconnectable
import rescala.default.*
import webapp.given_ExecutionContext
import scala.concurrent.*
import scala.annotation.nowarn

object Seqnal {

  implicit class SignalOps[T](self: Signal[T]) {

    def toFuture: Future[T] = {
      val promise = Promise[T]()
      val disconnectable: Disconnectable = self.observe(v => {
        promise.success(v): @nowarn("msg=discarded expression")
      })
      promise.future.map(v => {
        disconnectable.disconnect()
        v
      })
    }

    def waitUntil(pred: T => Boolean): Future[T] = {
      val promise = Promise[T]()
      val disconnectable: Disconnectable = self.observe(v => {
        if (pred(v)) {
          promise.success(v): @nowarn("msg=discarded expression")
        }
      })
      promise.future.map(v => {
        disconnectable.disconnect()
        v
      })
    }

  }

  implicit class SeqOfSignalOps[T](self: Seq[Signal[T]]) {

    def seqToSignal: Signal[Seq[T]] = Signal(self).flatten
  }

  implicit class SigOfSeq[T](self: Signal[Seq[T]]) {

    def mapInside[U](f: T => U): Signal[Seq[U]] = self.map(_.map(f))
  }

  implicit class SeqOps[T](self: Seq[T]) {

    def filterSignal(p: T => Signal[Boolean]): Signal[Seq[T]] = self
      .map(e => p(e).map(if (_) Seq(e) else Seq.empty))
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
