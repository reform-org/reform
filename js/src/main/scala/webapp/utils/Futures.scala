package webapp.utils

import webapp.services.Toaster

import scala.concurrent.Future

import concurrent.ExecutionContext.Implicits.global

object Futures {

  implicit class FutureOps[T](self: Future[T]) {

    def toastOnError(using toaster: Toaster): Unit = {
      self
        .onComplete(value => {
        if (value.isFailure) {
          value.failed.get.printStackTrace()
          toaster.make(value.failed.get.getMessage.nn)
        }
      })
    }
  }
}
