package webapp.entity

import webapp.utils.Seqnal.*
import rescala.default.*

import webapp.components.common.*
import webapp.services.RoutingService
import webapp.npm.JSUtils
import webapp.services.Page

case class UIAttributeBuilder[AttributeType](
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    label: String = "",
    isRequired: Boolean = false,
    min: String = "",
    stepSize: String = "1",
    regex: String = ".*",
    fieldType: String = "text",
    editConverter: AttributeType => String = (a: AttributeType) => a.toString,
    options: Signal[Seq[(String, Signal[String])]] = Signal(Seq.empty),
    searchEnabled: Boolean = true,
    createPage: Option[Page] = None,
)(using routing: RoutingService) {

  def withLabel(label: String): UIAttributeBuilder[AttributeType] = copy(label = label)

  def withMin(min: String): UIAttributeBuilder[AttributeType] = copy(min = min)

  def withFieldType(fieldType: String): UIAttributeBuilder[AttributeType] = copy(fieldType = fieldType)

  def require: UIAttributeBuilder[AttributeType] = copy(isRequired = true)

  def withStep(step: String): UIAttributeBuilder[AttributeType] = copy(stepSize = step)

  def withRegex(regex: String): UIAttributeBuilder[AttributeType] = copy(regex = regex)

  def disableSearch: UIAttributeBuilder[AttributeType] = copy(searchEnabled = false)

  def withCreatePage(page: Page): UIAttributeBuilder[AttributeType] = copy(createPage = Some(page))

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
  ): UIAttribute[EntityType, AttributeType] = UITextAttribute(
    getter = getter,
    setter = setter,
    readConverter = readConverter,
    editConverter = editConverter,
    writeConverter = writeConverter,
    label = label,
    isRequired = isRequired,
    regex = regex,
    stepSize = stepSize,
    fieldType = fieldType,
  )

}

object UIAttributeBuilder {

  def string(using routing: RoutingService): UIAttributeBuilder[String] = UIAttributeBuilder(identity, identity)

  def date(using routing: RoutingService): UIAttributeBuilder[Long] = UIAttributeBuilder(
    JSUtils.toGermanDate,
    writeConverter = JSUtils.DateTimeFromISO,
  )

  def int(using routing: RoutingService): UIAttributeBuilder[Int] = UIAttributeBuilder[Int](_.toString, _.toInt)

  def email(using routing: RoutingService): UIAttributeBuilder[String] = string.withFieldType("email")

  def float(using routing: RoutingService): UIAttributeBuilder[Float] = UIAttributeBuilder[Float](_.toString, _.toFloat)

  def boolean(using routing: RoutingService): UIAttributeBuilder[Boolean] = UIAttributeBuilder(_.toString, _.toBoolean)

  def money(using routing: RoutingService): UIAttributeBuilder[BigDecimal] =
    UIAttributeBuilder[BigDecimal](JSUtils.toMoneyString(_), BigDecimal(_), editConverter = _.toString)
      .withStep("0.01")
      .withRegex("\\d*(\\.\\d\\d?)?")

  def select(options: Signal[Seq[(String, Signal[String])]])(using
      routing: RoutingService,
  ): UIAttributeBuilder[String] =
    string.copy(options = options)

  def multiSelect(
      options: Signal[Seq[(String, Signal[String])]],
  )(using routing: RoutingService): UIAttributeBuilder[Seq[String]] =
    UIAttributeBuilder[Seq[String]](r => r.mkString(", "), w => w.split(", ").nn.map(_.nn).toSeq)
      .copy(options = options)

  implicit class BindToInt[AttributeType](self: UIAttributeBuilder[AttributeType])(implicit
      ordering: Ordering[AttributeType],
  ) {
    def bindAsNumber[EntityType](
        getter: EntityType => Attribute[AttributeType],
        setter: (EntityType, Attribute[AttributeType]) => EntityType,
    )(using routing: RoutingService): UIAttribute[EntityType, AttributeType] = UINumberAttribute(
      getter = getter,
      setter = setter,
      readConverter = self.readConverter,
      editConverter = self.editConverter,
      writeConverter = self.writeConverter,
      label = self.label,
      isRequired = self.isRequired,
      regex = self.regex,
      min = self.min,
      stepSize = self.stepSize,
    )
  }

  implicit class BindToLong(self: UIAttributeBuilder[Long]) {
    def bindAsDatePicker[EntityType](
        getter: EntityType => Attribute[Long],
        setter: (EntityType, Attribute[Long]) => EntityType,
    )(using routing: RoutingService): UIAttribute[EntityType, Long] = UIDateAttribute(
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
    )(using routing: RoutingService): UIAttribute[EntityType, Boolean] = UICheckboxAttribute(
      getter = getter,
      setter = setter,
      label = self.label,
      isRequired = self.isRequired,
    )
  }

  implicit class BindToSeqOfString(self: UIAttributeBuilder[Seq[String]]) {

    def bindAsMultiSelect[EntityType](
        getter: EntityType => Attribute[Seq[String]],
        setter: (EntityType, Attribute[Seq[String]]) => EntityType,
    )(using routing: RoutingService): UIAttribute[EntityType, Seq[String]] =
      UIMultiSelectAttribute(
        getter,
        setter,
        readConverter = self.readConverter,
        writeConverter = self.writeConverter,
        label = self.label,
        options = self.options.mapInside { case (k, v) => MultiSelectOption(k, v) },
        isRequired = self.isRequired,
        searchEnabled = self.searchEnabled,
        createPage = self.createPage,
      )
  }

  implicit class BindToString(self: UIAttributeBuilder[String]) {

    def bindAsSelect[EntityType](
        getter: EntityType => Attribute[String],
        setter: (EntityType, Attribute[String]) => EntityType,
    )(using routing: RoutingService): UIAttribute[EntityType, String] =
      UISelectAttribute(
        getter,
        setter,
        readConverter = self.readConverter,
        writeConverter = self.writeConverter,
        label = self.label,
        options = self.options.mapInside { case (k, v) => SelectOption(k, v) },
        isRequired = self.isRequired,
        searchEnabled = self.searchEnabled,
        createPage = self.createPage,
      )

  }
}
