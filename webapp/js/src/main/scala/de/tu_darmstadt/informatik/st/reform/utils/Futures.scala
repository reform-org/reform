package de.tu_darmstadt.informatik.st.reform.utils

import de.tu_darmstadt.informatik.st.reform.services.Toaster

import scala.concurrent.Future

import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.services.ToastMode
import de.tu_darmstadt.informatik.st.reform.services.ToastType
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import de.tu_darmstadt.informatik.st.reform.JSImplicits

object Futures {

  implicit class FutureOps[T](self: Future[T]) {

    def toastOnError(using
        jsImplicits: JSImplicits,
    )(mode: ToastMode = ToastMode.Short, style: ToastType = ToastType.Error): Unit = {
      self
        .onComplete(value => {
          if (value.isFailure) {
            value.failed.get.printStackTrace()
            jsImplicits.toaster.make(value.failed.get.getMessage.nn, mode, style)
          }
        })
    }
  }

  implicit class TryOps[T](self: Try[T]) {
    def toastOnError(mode: ToastMode = ToastMode.Short, style: ToastType = ToastType.Error)(using
        jsImplicits: JSImplicits,
    ): Unit = {
      self match {
        case Success(value) =>
        case Failure(exception) =>
          exception.printStackTrace()
          jsImplicits.toaster.make(exception.getMessage.nn, mode, style)
      }
    }
  }
}
