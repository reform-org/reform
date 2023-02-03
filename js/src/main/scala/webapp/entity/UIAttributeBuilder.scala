package webapp.entity

import webapp.utils.Date

case class UIAttributeBuilder[AttributeType](
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    fieldType: String = "text",
    label: String = "",
    isRequired: Boolean = false,
    min: String = "",
) {

  def withLabel(label: String): UIAttributeBuilder[AttributeType] = copy(label = label)

  def withFieldType(fieldType: String): UIAttributeBuilder[AttributeType] = copy(fieldType = fieldType)

  def withMin(min: String): UIAttributeBuilder[AttributeType] = copy(min = min)

  def require: UIAttributeBuilder[AttributeType] = copy(isRequired = true)

  def withDefaultValue(default: AttributeType): UIAttributeBuilder[Option[AttributeType]] =
    map(_.getOrElse(default), Some(_))

  def map[NewAttributeType](
      readMapper: NewAttributeType => AttributeType,
      writeMapper: AttributeType => NewAttributeType,
  ): UIAttributeBuilder[NewAttributeType] = copy(
    readConverter = a => readConverter(readMapper(a)),
    writeConverter = s => writeMapper(writeConverter(s)),
  )

  def bind[EntityType](
      getter: EntityType => Attribute[AttributeType],
      setter: (EntityType, Attribute[AttributeType]) => EntityType,
  ): UIAttribute[EntityType, AttributeType] = UIAttribute(
    getter = getter,
    setter = setter,
    readConverter = readConverter,
    writeConverter = writeConverter,
    label = label,
    isRequired = isRequired,
    fieldType = fieldType,
  )

}

object UIAttributeBuilder {

  val string: UIAttributeBuilder[String] = UIAttributeBuilder(identity, identity)

  val date: UIAttributeBuilder[Long] = UIAttributeBuilder(
    Date.epochDayToDate(_, "dd.MM.yyyy"),
    writeConverter = Date.dateToEpochDay(_, "yyyy-MM-dd"),
  )

  val int: UIAttributeBuilder[Int] = UIAttributeBuilder[Int](_.toString, _.toInt)
    .withFieldType("number")

  val float: UIAttributeBuilder[Float] = UIAttributeBuilder[Float](_.toString, _.toFloat)
    .withFieldType("number")

  val boolean: UIAttributeBuilder[Boolean] = UIAttributeBuilder(_.toString, _.toBoolean)

  implicit class BindToDate(self: UIAttributeBuilder[Long]) {
    def bindAsDatePicker[EntityType](
        getter: EntityType => Attribute[Long],
        setter: (EntityType, Attribute[Long]) => EntityType,
    ): UIAttribute[EntityType, Long] = UIDateAttribute(
      getter = getter,
      setter = setter,
      readConverter = self.readConverter,
      writeConverter = self.writeConverter,
      label = self.label,
      min = self.min,
      isRequired = self.isRequired,
    )
  }

  implicit class BindToBoolean(self: UIAttributeBuilder[Boolean]) {
    def bindAsCheckbox[EntityType](
        getter: EntityType => Attribute[Boolean],
        setter: (EntityType, Attribute[Boolean]) => EntityType,
    ): UIAttribute[EntityType, Boolean] = UICheckboxAttribute(
      getter = getter,
      setter = setter,
      label = self.label,
      isRequired = self.isRequired,
    )
  }
}
