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

  @js.native
  @JSImport("../../../../utils.js", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def usesTurn(connection: js.Object): js.Promise[Boolean] =
      js.native
  }
}