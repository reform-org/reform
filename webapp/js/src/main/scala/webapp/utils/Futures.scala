package webapp.utils

import webapp.services.Toaster

import scala.concurrent.Future

import webapp.given_ExecutionContext
import webapp.services.ToastMode
import webapp.services.ToastType
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import webapp.JSImplicits

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
