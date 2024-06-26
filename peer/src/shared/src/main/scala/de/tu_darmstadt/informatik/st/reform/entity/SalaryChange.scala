package de.tu_darmstadt.informatik.st.reform.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonReader
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriter
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*
import kofre.base.*

case class SalaryChange(
    value: Attribute[BigDecimal] = Attribute.empty,
    limit: Attribute[BigDecimal] = Attribute.empty,
    paymentLevel: Attribute[String] = Attribute.empty,
    fromDate: Attribute[Long] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[SalaryChange]
    derives Lattice,
      Bottom {

  // empty for required fields, default for optional fields
  def default: SalaryChange =
    SalaryChange(Attribute.empty, Attribute.empty, Attribute.empty, Attribute.empty, Attribute(true))

  def identifier: Attribute[String] = paymentLevel

  def withExists(exists: Boolean): SalaryChange = {
    this.copy(_exists = _exists.set(exists))
  }

  override def exists: Boolean = _exists.getOrElse(true)
}

object SalaryChange {
  val empty: SalaryChange = SalaryChange()

  implicit val bigDecimalCodec: JsonValueCodec[BigDecimal] = new JsonValueCodec[BigDecimal] {
    def decodeValue(in: JsonReader, default: BigDecimal): BigDecimal = in.readBigDecimal(0)

    def encodeValue(x: BigDecimal, out: JsonWriter): Unit = out.writeVal(x)

    def nullValue: BigDecimal = null.asInstanceOf[BigDecimal]
  }

  implicit val codec: JsonValueCodec[SalaryChange] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))
}
