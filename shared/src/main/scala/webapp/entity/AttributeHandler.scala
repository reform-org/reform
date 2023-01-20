package webapp.entity

case class AttributeHandler[EntityType, AttributeType](
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    setter: (EntityType, AttributeType) => EntityType,
)

object AttributeHandler {

  def string[EntityType](setter: (EntityType, String) => EntityType): AttributeHandler[EntityType, String]
  = AttributeHandler(identity, identity, setter)

  def int[EntityType](setter: (EntityType, Int) => EntityType): AttributeHandler[EntityType, Int]
  = AttributeHandler(_.toString, _.toInt, setter)

  def optionWithDefault[EntityType, AttributeType](default: AttributeType, attr: AttributeHandler[EntityType, AttributeType]): AttributeHandler[EntityType, Option[AttributeType]]
  = AttributeHandler(
    v => attr.readConverter(v.getOrElse(default)),
    s => Some(attr.writeConverter(s)),
    (e, v) => attr.setter(e, v.getOrElse(default))
  )
}
