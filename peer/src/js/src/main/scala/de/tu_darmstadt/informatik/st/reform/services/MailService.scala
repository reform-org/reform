package de.tu_darmstadt.informatik.st.reform.services

import cats.effect.SyncIO
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import de.tu_darmstadt.informatik.st.reform.Globals
import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.entity.Hiwi
import de.tu_darmstadt.informatik.st.reform.entity.Supervisor
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.toGermanDate
import de.tu_darmstadt.informatik.st.reform.utils.Futures.*
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
class MailService {

  class MailBody(
      val to: String,
      val from: String,
      val fromName: String,
      val html: String,
      val subject: String,
      val attachments: Seq[MailAttachment],
      val bcc: Seq[String],
  ) {}
  private object MailBody {
    val codec: JsonValueCodec[MailBody] = JsonCodecMaker.make
  }

  class MailAnswer(val accepted: Seq[String], val rejected: Seq[String]) {}

  def sendMail(using jsImplicits: JSImplicits)(
      to: String,
      from: String,
      fromName: String,
      mail: Mail,
      bcc: Seq[String] = Seq.empty,
  ): Future[MailAnswer] = {
    val promise = Promise[MailAnswer]()
    val element = document.createElement("div")
    Outwatch.renderInto[SyncIO](element, mail.body).unsafeRunSync()
    val htmlString = element.innerHTML

    if (jsImplicits.discovery.tokenIsValid(jsImplicits.discovery.token.now)) {
      val requestHeaders = new Headers()
      requestHeaders.set("content-type", "application/json")
      requestHeaders.set("authorization", s"Bearer ${jsImplicits.discovery.token.now.getOrElse("")}")
      fetch(
        Globals.DISCOVERY_SERVER_URL + "/mail",
        new RequestInit {
          method = HttpMethod.POST
          body =
            writeToString(MailBody(to, from, fromName, htmlString, mail.subject, mail.attachments, bcc))(MailBody.codec)
          headers = requestHeaders
        },
      ).`then`(s => {
        s.json()
          .toFuture
          .onComplete(json => {
            if (s.status > 400 && s.status < 500) {
              val error = json.get.asInstanceOf[js.Dynamic].error
              promise.failure(
                new LoginException(
                  error.message.asInstanceOf[String],
                  error.fields.asInstanceOf[js.Array[String]].toSeq,
                ),
              )
            } else {
              val response = json.get.asInstanceOf[js.Dynamic]
              promise.success(
                MailAnswer(
                  response.selectDynamic("accepted").asInstanceOf[js.Array[String]].toSeq,
                  response.selectDynamic("rejected").asInstanceOf[js.Array[String]].toSeq,
                ),
              )
            }
          })
      }).toFuture
        .toastOnError()
    }

    promise.future
  }
}

class MailAttachment(
    val filename: String,
    val content: ArrayBuffer[Short],
    val contentType: String = "application/pdf",
) {}

/** Please note the following: In order for emails to be displayed correctly:
  *   - all images are absolute links (https://...)
  *   - all styles be inline (with styleAttr)
  */

abstract class Mail(val subject: String) {
  val body: VNode
  val attachments: Seq[MailAttachment] = Seq.empty
}

class ReminderMail(hiwi: Hiwi, supervisor: Supervisor, due: Long, missingDocuments: Seq[String])
    extends Mail("Reminder") {
  val body: VNode = div(
    p("Hallo ", hiwi.firstName.get.getOrElse(""), " ", hiwi.lastName.get.getOrElse(""), ","),
    p("Wir möchten Sie freundlich daran erinnern Ihre Unterlagen bis zum ", i(toGermanDate(due)), " einzureichen."),
    p("Es fehlen noch folgende Unterlage(n): ", ol(missingDocuments.map(li(_)))),
    Signature(supervisor.name.get.getOrElse("")),
  )
}

class DekanatMail(hiwi: Hiwi, supervisor: Supervisor, letter: ArrayBuffer[Short])
    extends Mail("Hiwistelle Software Technology Group") {
  val body: VNode = div(
    p("Sehr geehrte Damen und Herren, "),
    p(
      "Im Anhang finden Sie ein förmliches Anschreiben bezüglich der Anstellung des Hiwis ",
      hiwi.firstName.get.getOrElse(""),
      " ",
      hiwi.lastName.get.getOrElse(""),
      ".",
    ),
    Signature(supervisor.name.get.getOrElse("")),
  )

  override val attachments: Seq[MailAttachment] = Seq(
    new MailAttachment(
      s"Anstellung-${hiwi.firstName.get.getOrElse("")}-${hiwi.lastName.get.getOrElse("")}.pdf",
      letter,
      "application/pdf",
    ),
  )
}

class ContractEmail(hiwi: Hiwi, supervisor: Supervisor, due: Long, contract: ArrayBuffer[Short])
    extends Mail("Hiwistelle Software Technology Group") {
  val body: VNode = div(
    p("Hallo ", hiwi.firstName.get.getOrElse(""), " ", hiwi.lastName.get.getOrElse(""), ","),
    p(
      "Im Anhang findest du den Arbeitsvertrag. Bitte schicke uns den Vertrag ausgefüllt bis zum ",
      i(toGermanDate(due)),
      " zurück.",
    ),
    p("Vielen Dank!"),
    Signature(supervisor.name.get.getOrElse("")),
  )

  override val attachments: Seq[MailAttachment] = Seq(
    new MailAttachment(
      s"Vertrag-${hiwi.firstName.get.getOrElse("")}-${hiwi.lastName.get.getOrElse("")}.pdf",
      contract,
      "application/pdf",
    ),
  )
}

object Signature {
  def apply(name: String): VNode = {
    p(
      "Mit freundlichen Grüßen,",
      br,
      name,
      br,
      "--",
      br,
      "Software Technology Group",
      br,
      "Hochschulstraße 10",
      br,
      "64289 Darmstadt",
      br,
      a(href := "https://www.stg.tu-darmstadt.de/", "https://www.stg.tu-darmstadt.de/"),
    )
  }
}
