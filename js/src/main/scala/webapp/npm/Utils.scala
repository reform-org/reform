package webapp.npm

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.{JSON, Promise}
import scala.scalajs.js.annotation.JSImport
import concurrent.ExecutionContext.Implicits.global
import js.JSConverters.*
import org.scalajs.dom.RTCPeerConnection

object Utils {

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
