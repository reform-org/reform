package webapp.entity

import webapp.utils.Date

case class UIAttributeBuilder[AttributeType](
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    placeholder: String = "Placeholder value",
    fieldType: String = "text",
    min: String = "",
) {

  def withPlaceholder(placeholder: String): UIAttributeBuilder[AttributeType] = copy(placeholder = placeholder)

  def withFieldType(fieldType: String): UIAttributeBuilder[AttributeType] = copy(fieldType = fieldType)

  def withMin(min: String): UIAttributeBuilder[AttributeType] = copy(min = min)

  def withDefaultValue(default: AttributeType): UIAttributeBuilder[Option[AttributeType]] = copy(
    readConverter = a => readConverter(a.getOrElse(default)),
    writeConverter = s => Some(writeConverter(s)),
  )

  def bind[EntityType](
      getter: EntityType => Attribute[AttributeType],
      setter: (EntityType, Attribute[AttributeType]) => EntityType,
  ): UICommonAttribute[EntityType, AttributeType] =
    UIAttribute(getter, setter, readConverter, writeConverter, placeholder, fieldType)

}

class UIDateAttributeBuilder(
) extends UIAttributeBuilder[Long](
      readConverter = Date.epochDayToDate(_, "dd.MM.yyyy"),
      writeConverter = Date.dateToEpochDay(_, "yyyy-MM-dd"),
    ) {

  override def bind[EntityType](
      getter: EntityType => Attribute[Long],
      setter: (EntityType, Attribute[Long]) => EntityType,
  ): UICommonAttribute[EntityType, Long] =
    UIDateAttribute(getter, setter, readConverter, writeConverter, placeholder, min)
}

object UIAttributeBuilder {

  val string: UIAttributeBuilder[String] = UIAttributeBuilder(identity, identity)

  val date: UIAttributeBuilder[Long] = UIDateAttributeBuilder()

  val int: UIAttributeBuilder[Int] = UIAttributeBuilder[Int](_.toString, _.toInt)
    .withFieldType("number")
}
