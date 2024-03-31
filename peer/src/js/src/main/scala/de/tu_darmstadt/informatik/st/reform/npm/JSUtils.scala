package de.tu_darmstadt.informatik.st.reform.npm

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

  def cleanPopper(trigger: String): Unit = {
    NativeImpl.cleanPopper(trigger)
  }

  def stickyButton(trigger: String, element: String, toggleClass: String): Unit =
    NativeImpl.stickyButton(trigger, element, toggleClass)

  def cleanStickyButtons(): Unit = NativeImpl.cleanStickyButtons()

  def toGermanDate(input: Long): String = NativeImpl.toGermanDate(input.toString)
  def getMonth(input: Long): Int = NativeImpl.getMonth(input.toString)
  def getYear(input: Long): Int = NativeImpl.getYear(input.toString)
  def toMilliseconds(month: Int, year: Int): Long = NativeImpl.toMilliseconds(month, year)

  def toHumanMonth(input: Int): String = NativeImpl.toHumanMonth(input)

  def DateTimeFromISO(input: String): Long = NativeImpl.DateTimeFromISO(input).toLong

  def toYYYYMMDD(input: Long): String = NativeImpl.toYYYYMMDD(input.toString)

  def dateDiffDays(a: Long, b: Long): Int = NativeImpl.dateDiffDays(a.toString, b.toString)

  def dateDiffMonth(a: Long, b: Long): Int = NativeImpl.dateDiffMonth(a.toString, b.toString)

  def dateDiffHumanReadable(a: Long, b: Long): String = NativeImpl.dateDiffHumanReadable(a.toString, b.toString)

  def dateAdd(date: Long, days: Long = 0, months: Long = 0, years: Long = 0): Long =
    NativeImpl.dateAdd(date.toString, days.toString, months.toString, years.toString).toLong

  val toMoneyString: BigDecimal => String = NativeImpl.toMoneyString

  @js.native
  @JSImport("/utils.js", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def usesTurn(connection: js.Object): js.Promise[Boolean] =
      js.native

    def downloadFile(name: String, content: String, tpe: String): Unit =
      js.native

    def createPopper(trigger: String, element: String, placement: String, sameWidth: Boolean): Unit =
      js.native

    def cleanPopper(trigger: String): Unit = js.native

    def stickyButton(trigger: String, element: String, toggleClass: String): Unit = js.native

    def cleanStickyButtons(): Unit = js.native

    def toGermanDate(input: String): String = js.native

    def toHumanMonth(input: Int): String = js.native

    def getYear(input: String): Int = js.native
    def getMonth(input: String): Int = js.native
    def toMilliseconds(month: Int, year: Int): Long = js.native
    def toYYYYMMDD(input: String): String = js.native

    def DateTimeFromISO(input: String): String = js.native

    def dateDiffDays(a: String, b: String): Int = js.native

    def dateDiffMonth(a: String, b: String): Int = js.native

    def dateDiffHumanReadable(a: String, b: String): String = js.native

    def dateAdd(date: String, deltaDays: String, deltaMonths: String, deltaYears: String): String = js.native

    def toMoneyString(input: BigDecimal): String = js.native
  }
}
