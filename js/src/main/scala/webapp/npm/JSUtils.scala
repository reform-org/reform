package webapp.npm

import org.scalajs.dom.RTCPeerConnection

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

import js.JSConverters.*

object JSUtils {

  def usesTurn(connection: RTCPeerConnection): Future[Boolean] = {
    val promise: js.Promise[Boolean] = NativeImpl.usesTurn(connection.asInstanceOf[js.Object])
    promise.toFuture
  }

  def downloadFile(name: String, content: String, tpe: String): Unit = {
    NativeImpl.downloadFile(name, content, tpe)
  }

  def createPopper(trigger: String, element: String, placement: String = "bottom", sameWidth: Boolean = true): Unit = {
    NativeImpl.createPopper(trigger, element, placement, sameWidth)
  }

  def cleanPopper(): Unit = {
    NativeImpl.cleanPopper()
  }

  val isSelenium: Boolean = NativeImpl.isSelenium

  def toGermanDate(input: Long) = NativeImpl.toGermanDate(input.toString())

  def DateTimeFromISO(input: String) = NativeImpl.DateTimeFromISO(input).toLong

  def toYYYYMMDD(input: Long) = NativeImpl.toYYYYMMDD(input.toString())

  val toMoneyString = NativeImpl.toMoneyString

  @js.native
  @JSImport("../../../utils.js", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def usesTurn(connection: js.Object): js.Promise[Boolean] =
      js.native

    def downloadFile(name: String, content: String, tpe: String): Unit =
      js.native

    def createPopper(trigger: String, element: String, placement: String, sameWidth: Boolean): Unit =
      js.native

    def cleanPopper(): Unit =
      js.native

    val isSelenium: Boolean = js.native

    def toGermanDate(input: String): String = js.native

    def toYYYYMMDD(input: String): String = js.native

    def DateTimeFromISO(input: String): String = js.native

    def toMoneyString(input: BigDecimal): String = js.native
  }
}
