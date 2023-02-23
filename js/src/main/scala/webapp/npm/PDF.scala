package webapp.npm

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

import js.JSConverters.*

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

  def fill(inputFileURI: String, outputFileName: String, fields: Seq[PDFField]): Future[String] = {
    val promise: js.Promise[String] = NativeImpl.fillPDF(
      inputFileURI,
      outputFileName,
      fields
        .map(field =>
          js.Dynamic.literal(fieldType = field.fieldType, key = field.key, value = field.value.asInstanceOf[js.Any]),
        )
        .toJSArray,
    )
    promise.toFuture
  }

  @js.native
  @JSImport("../../../pdf.js", JSImport.Namespace)
  private object NativeImpl extends js.Object {

    def fillPDF(inputFileURI: String, outputFileName: String, fields: js.Array[js.Object]): js.Promise[String] =
      js.native
  }
}
