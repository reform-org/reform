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
    val required: Boolean,
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
    td(duplicateValuesHandler(attr.getAll.map(x => readConverter(x))))
  }

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]): Signal[Option[outwatch.VNode]]
}

case class UIAttribute[EntityType, AttributeType](
    override val getter: EntityType => Attribute[AttributeType],
    override val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    override val readConverter: AttributeType => String,
    override val writeConverter: String => AttributeType,
    override val placeholder: String,
    override val required: Boolean,
    fieldType: String,
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      placeholder,
      required,
    ) {

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]) = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          input(
            cls := "input valid:input-success",
            `type` := fieldType,
            VModifier.attr("form") := formAttr, // TODO FIXME check browser support
            VModifier.attr("required") := required,
            value := attr.get.map(x => readConverter(x)).getOrElse(""),
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
            VModifier.attr("placeholder") := placeholder,
          ),
          if (attr.getAll.size > 1) {
            Some(
              p(
                "Conflicting values: ",
                attr.getAll.map(x => readConverter(x)).mkString("/"),
              ),
            )
          } else {
            None
          },
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
    override val required: Boolean,
    editConverter: AttributeType => String,
    min: String = "",
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      placeholder,
      required,
    ) {

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]) = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          input(
            cls := "input valid:input-success",
            `type` := "date",
            VModifier.attr("form") := formAttr, // TODO FIXME check browser support
            VModifier.attr("required") := required,
            minAttr := min,
            value := attr.get.map(x => editConverter(x)).getOrElse(""),
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
          ),
          if (attr.getAll.size > 1) {
            Some(
              p(
                "Conflicting values: ",
                attr.getAll.map(x => readConverter(x)).mkString("/"),
              ),
            )
          } else {
            None
          },
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
    override val required: Boolean,
    options: Signal[List[UIOption[Signal[String]]]],
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      placeholder,
      required,
    ) {

  override def render(entity: EntityType) = {
    val attr = getter(entity)
    td(duplicateValuesHandler(attr.getAll.map(x => options.map(o => o.filter(p => p.id == x).map(v => v.name)))))
  }

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]) = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          select(
            cls := "input valid:input-success",
            VModifier.attr("form") := formAttr, // TODO FIXME check browser support
            VModifier.attr("required") := required,
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
            option(VModifier.attr("value") := "", "Bitte wählen..."),
            options.map(o =>
              o.map(v => option(value := v.id, selected := Some(v.id) == attr.get.map(x => readConverter(x)), v.name)),
            ),
          ),
          if (attr.getAll.size > 1) {
            Some(
              p(
                "Conflicting values: ", {
                  options
                    .map(o => o.filter(p => attr.getAll.map(readConverter).contains(p.id)).map(v => v.name))
                    .flatten
                    .map(v => v.mkString("/"))
                },
              ),
            )
          } else {
            None
          },
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
