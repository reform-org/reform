package webapp.npm

import org.scalajs.dom.RTCPeerConnection

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

import js.JSConverters.*

object Utils {

  def usesTurn(connection: RTCPeerConnection): Future[Boolean] = {
    val promise: js.Promise[Boolean] = NativeImpl.usesTurn(connection.asInstanceOf[js.Object])
    promise.toFuture
  }

  def downloadJson(name: String, content: String): Unit = {
    NativeImpl.downloadJson(name, content)
  }

  @js.native
  @JSImport("../../../../utils.js", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def usesTurn(connection: js.Object): js.Promise[Boolean] =
      js.native

    def downloadJson(name: String, content: String): Unit =
      js.native
  }
}
