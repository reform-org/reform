package webapp.entity

case class UIAttributeBuilder[AttributeType](
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    placeholder: String = "Placeholder value",
) {

  def withPlaceholder(placeholder: String): UIAttributeBuilder[AttributeType] =
    copy(placeholder = placeholder)

  def withDefaultValue(default: AttributeType): UIAttributeBuilder[Option[AttributeType]]
    = copy(
      readConverter = a => readConverter(a.getOrElse(default)),
      writeConverter = s => Some(writeConverter(s)),
    )

  def bind[EntityType](
      getter: EntityType => Attribute[AttributeType],
      setter: (EntityType, Attribute[AttributeType]) => EntityType,
  ): UIAttribute[EntityType, AttributeType] = UIAttribute(getter, setter, readConverter, writeConverter, placeholder)

}

object UIAttributeBuilder {

  val string: UIAttributeBuilder[String]
    = UIAttributeBuilder(identity, identity)

  val int: UIAttributeBuilder[Int]
    = UIAttributeBuilder(_.toString, _.toInt)
}
