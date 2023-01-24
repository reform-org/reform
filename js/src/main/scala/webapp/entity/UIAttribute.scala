package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import org.scalajs.dom.*
import webapp.entity.Attribute
import webapp.Repositories
import webapp.given
import webapp.duplicateValuesHandler

class UIOption[NameType](
    val id: String,
    val name: NameType,
) {}

// contains attributes and methods that are used by all UIAttributes
abstract class UICommonAttribute[EntityType, AttributeType](
    val getter: EntityType => Attribute[AttributeType],
    val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    val readConverter: AttributeType => String,
    val writeConverter: String => AttributeType,
    val placeholder: String,
) {
  def setFromString(entityVar: Var[Option[EntityType]], x: String): Unit = {
    entityVar.transform(
      _.map(e => {
        val attr = getter(e)
        setter(e, attr.set(writeConverter(x)))
      }),
    )
  }

  def render(entity: EntityType) = {
    val attr = getter(entity)
    td(cls:="px-6 py-4", duplicateValuesHandler(attr.getAll.map(x => readConverter(x))))
  }

  def renderEdit(entityVar: Var[Option[EntityType]]): Signal[Option[outwatch.VNode]]
}

case class UIAttribute[EntityType, AttributeType](
    override val getter: EntityType => Attribute[AttributeType],
    override val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    override val readConverter: AttributeType => String,
    override val writeConverter: String => AttributeType,
    override val placeholder: String,
    fieldType: String = "text",
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      placeholder,
    ) {

  def renderEdit(entityVar: Var[Option[EntityType]]) = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls:="px-6 py-4", 
          input(
            tpe := fieldType,
            value := attr.getAll.map(x => readConverter(x)).mkString("/"),
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
            VModifier.prop("placeholder") := placeholder,
          ),
        )
      })
    }
  }
}

case class UIDateAttribute[EntityType, AttributeType](
    override val getter: EntityType => Attribute[AttributeType],
    override val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    override val readConverter: AttributeType => String,
    override val writeConverter: String => AttributeType,
    override val placeholder: String,
    editConverter: AttributeType => String,
    min: String = "",
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      placeholder,
    ) {

  def renderEdit(entityVar: Var[Option[EntityType]]) = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls:="px-6 py-4", 
          input(
            tpe := "date",
            minAttr := min,
            value := attr.getAll.map(x => editConverter(x)).mkString("/"),
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
          ),
        )
      })
    }
  }
}

case class UISelectAttribute[EntityType, AttributeType](
    override val getter: EntityType => Attribute[AttributeType],
    override val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    override val readConverter: AttributeType => String,
    override val writeConverter: String => AttributeType,
    override val placeholder: String,
    options: Signal[List[UIOption[Signal[String]]]],
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      placeholder,
    ) {

  override def render(entity: EntityType) = {
    val attr = getter(entity)
    td(cls:="px-6 py-4", duplicateValuesHandler(attr.getAll.map(x => options.map(o => o.filter(p => p.id == x).map(v => v.name)))))
  }

  def renderEdit(entityVar: Var[Option[EntityType]]) = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls:="px-6 py-4", 
          select(
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
            option(value := "", "Bitte wählen..."),
            options.map(o => o.map(v => option(value := v.id, v.name))),
          ),
        )
      })
    }
  }
}

object UIAttribute {

  /* def string[EntityType](setter: (EntityType, String) => EntityType): AttributeHandler[EntityType, String]
  = AttributeHandler(identity, identity, setter)

  def int[EntityType](setter: (EntityType, Int) => EntityType): AttributeHandler[EntityType, Int]
  = AttributeHandler(_.toString, _.toInt, setter)

  def optionWithDefault[EntityType, AttributeType](default: AttributeType, attr: AttributeHandler[EntityType, AttributeType]): AttributeHandler[EntityType, Option[AttributeType]]
  = AttributeHandler(
    v => attr.readConverter(v.getOrElse(default)),
    s => Some(attr.writeConverter(s)),
    (e, v) => attr.setter(e, v.getOrElse(default))
  ) */
}
