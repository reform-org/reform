package webapp.npm

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport
import webapp.given_ExecutionContext

import js.JSConverters.*
import scala.collection.mutable.ArrayBuffer

abstract class PDFField(
    val key: String,
) {
  val fieldType: String
  val value: Any
}

class PDFCheckboxField(
    override val key: String,
    override val value: Boolean,
) extends PDFField(key) {
  val fieldType = "checkbox"
}

class PDFTextField(
    override val key: String,
    override val value: String,
) extends PDFField(key) {
  val fieldType = "text"
}

object PDF {

  def fillAndDownload(
      inputFileURI: String,
      outputFileName: String,
      fields: Seq[PDFField],
  ): Future[ArrayBuffer[Short]] = {
    val promise: js.Promise[js.typedarray.Uint8Array] = NativeImpl.fillAndDownloadPDF(
      inputFileURI,
      outputFileName,
      fields
        .map(field =>
          js.Dynamic.literal(fieldType = field.fieldType, key = field.key, value = field.value.asInstanceOf[js.Any]),
        )
        .toJSArray,
    )
    promise.`then`(array => ArrayBuffer.from(array)).toFuture
  }

  def fill(inputFileURI: String, fields: Seq[PDFField]): Future[ArrayBuffer[Short]] = {
    val promise: js.Promise[js.typedarray.Uint8Array] = NativeImpl.fillPDF(
      inputFileURI,
      fields
        .map(field =>
          js.Dynamic.literal(fieldType = field.fieldType, key = field.key, value = field.value.asInstanceOf[js.Any]),
        )
        .toJSArray,
    )
    promise.`then`(array => ArrayBuffer.from(array)).toFuture
  }

  def download(outputFileName: String, bytes: ArrayBuffer[Short]) =
    NativeImpl.download(outputFileName, new js.typedarray.Uint8Array(bytes.toJSArray))

  @js.native
  @JSImport("../../../pdf.js", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def fillAndDownloadPDF(
        inputFileURI: String,
        outputFileName: String,
        fields: js.Array[js.Object],
    ): js.Promise[js.typedarray.Uint8Array] =
      js.native

    def fillPDF(inputFileURI: String, fields: js.Array[js.Object]): js.Promise[js.typedarray.Uint8Array] =
      js.native

    def download(outputFileName: String, bytes: js.typedarray.Uint8Array): Unit =
      js.native
  }
}
