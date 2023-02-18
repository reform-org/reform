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

  def downloadJson(name: String, content: String): Unit = {
    NativeImpl.downloadJson(name, content)
  }

  def createPopper(trigger: String, element: String): Unit = {
    NativeImpl.createPopper(trigger, element)
  }

  val isSelenium: Boolean = NativeImpl.isSelenium

  @js.native
  @JSImport("../../../../utils.js", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def usesTurn(connection: js.Object): js.Promise[Boolean] =
      js.native

    def downloadJson(name: String, content: String): Unit =
      js.native

    def createPopper(trigger: String, element: String): Unit =
      js.native

    val isSelenium: Boolean = js.native
  }
}
