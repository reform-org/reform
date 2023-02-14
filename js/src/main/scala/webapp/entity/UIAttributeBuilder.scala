package webapp.entity

import webapp.utils.Date

import webapp.utils.Money.*

case class UIAttributeBuilder[AttributeType](
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    label: String = "",
    isRequired: Boolean = false,
    min: String = "",
    stepSize: String = "1",
    regex: String = ".*",
    editConverter: AttributeType => String = (a: AttributeType) => a.toString,
) {

  def withLabel(label: String): UIAttributeBuilder[AttributeType] = copy(label = label)

  def withMin(min: String): UIAttributeBuilder[AttributeType] = copy(min = min)

  def require: UIAttributeBuilder[AttributeType] = copy(isRequired = true)

  def withStep(step: String): UIAttributeBuilder[AttributeType] = copy(stepSize = step)

  def withRegex(regex: String): UIAttributeBuilder[AttributeType] = copy(regex = regex)

  def withDefaultValue(default: AttributeType): UIAttributeBuilder[Option[AttributeType]] =
    map(_.getOrElse(default), Some(_))

  def map[NewAttributeType](
      readMapper: NewAttributeType => AttributeType,
      writeMapper: AttributeType => NewAttributeType,
  ): UIAttributeBuilder[NewAttributeType] = copy(
    readConverter = a => readConverter(readMapper(a)),
    writeConverter = s => writeMapper(writeConverter(s)),
    editConverter = a => editConverter(readMapper(a)),
  )

  def bindAsText[EntityType](
      getter: EntityType => Attribute[AttributeType],
      setter: (EntityType, Attribute[AttributeType]) => EntityType,
  ): UIAttribute[EntityType, AttributeType] = UIAttribute(
    getter = getter,
    setter = setter,
    readConverter = readConverter,
    editConverter = editConverter,
    writeConverter = writeConverter,
    label = label,
    isRequired = isRequired,
    regex = regex,
    stepSize = stepSize,
    fieldType = "text",
  )

}

object UIAttributeBuilder {

  val string: UIAttributeBuilder[String] = UIAttributeBuilder(identity, identity)

  val date: UIAttributeBuilder[Long] = UIAttributeBuilder(
    Date.epochDayToDate(_, "dd.MM.yyyy"),
    writeConverter = Date.dateToEpochDay(_, "yyyy-MM-dd"),
  )

  val int: UIAttributeBuilder[Int] = UIAttributeBuilder[Int](_.toString, _.toInt)

  val float: UIAttributeBuilder[Float] = UIAttributeBuilder[Float](_.toString, _.toFloat)

  val boolean: UIAttributeBuilder[Boolean] = UIAttributeBuilder(_.toString, _.toBoolean)

  val money: UIAttributeBuilder[BigDecimal] =
    UIAttributeBuilder[BigDecimal](_.toMoneyString, BigDecimal(_), editConverter = _.toString)
      .withStep("0.01")
      .withRegex("\\d*(\\.\\d\\d?)?")

  implicit class BindToInt[AttributeType](self: UIAttributeBuilder[AttributeType])(implicit
      ordering: Ordering[AttributeType],
  ) {
    def bindAsNumber[EntityType](
        getter: EntityType => Attribute[AttributeType],
        setter: (EntityType, Attribute[AttributeType]) => EntityType,
    ): UIAttribute[EntityType, AttributeType] = UINumberAttribute(
      getter = getter,
      setter = setter,
      readConverter = self.readConverter,
      editConverter = self.editConverter,
      writeConverter = self.writeConverter,
      label = self.label,
      isRequired = self.isRequired,
      regex = self.regex,
      stepSize = self.stepSize,
    )
  }

  implicit class BindToLong(self: UIAttributeBuilder[Long]) {
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
