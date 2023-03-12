package webapp.services

import webapp.webrtc.WebRTCService
import scala.concurrent.Future
import scala.concurrent.Promise
import org.scalajs.dom.*
import webapp.Globals
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import scala.scalajs.js
import webapp.given_ExecutionContext
import webapp.utils.Futures.*
import outwatch.*
import outwatch.dsl.*
import cats.effect.SyncIO
import webapp.JSImplicits
class MailService(using jsImplicits: JSImplicits) {

  class MailBody(val reciever: String, val replyTo: String, val html: String) {}
  object MailBody {
    val codec: JsonValueCodec[MailBody] = JsonCodecMaker.make
  }

  def sendMail(
      reciever: String,
      replyTo: String,
      html: VNode,
  ): Future[String] = {
    val promise = Promise[String]()
    val element = document.createElement("div")
    Outwatch.renderInto[SyncIO](element, html).unsafeRunSync()
    val htmlString = element.innerHTML

    if (jsImplicits.discovery.tokenIsValid(jsImplicits.discovery.token.now)) {
      val requestHeaders = new Headers();
      requestHeaders.set("content-type", "application/json");
      fetch(
        s"${Globals.VITE_DISCOVERY_SERVER_PROTOCOL}://${Globals.VITE_DISCOVERY_SERVER_HOST}:${Globals.VITE_DISCOVERY_SERVER_PUBLIC_PORT}${Globals.VITE_DISCOVERY_SERVER_PATH}/mail",
        new RequestInit {
          method = HttpMethod.POST
          body = writeToString(MailBody(reciever, replyTo, htmlString))(MailBody.codec)
          headers = requestHeaders
        },
      ).`then`(s => {
        s.json()
          .toFuture
          .onComplete(json => {
            if (s.status > 400 && s.status < 500) {
              val error = (json.get.asInstanceOf[js.Dynamic]).error;
              promise.failure(
                new jsImplicits.discovery.LoginException(
                  error.message.asInstanceOf[String],
                  error.fields.asInstanceOf[js.Array[String]].toSeq,
                ),
              )
            } else {
              val response = json.get.asInstanceOf[js.Dynamic]
              promise.success("")
            }
          })
      }).toFuture
        .toastOnError()
    }

    promise.future
  }
}
