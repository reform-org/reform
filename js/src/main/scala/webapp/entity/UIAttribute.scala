package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import org.scalajs.dom.*
import rescala.default
import webapp.entity.Attribute
import webapp.Repositories
import webapp.given
import webapp.duplicateValuesHandler
import webapp.utils.Date

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

  def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(duplicateValuesHandler(attr.getAll.map(x => readConverter(x))))
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

  def renderEdit(entityVar: Var[Option[EntityType]]): Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
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

case class UIDateAttribute[EntityType](
    override val getter: EntityType => Attribute[Long],
    override val setter: (EntityType, Attribute[Long]) => EntityType,
    override val readConverter: Long => String,
    override val writeConverter: String => Long,
    override val placeholder: String,
    min: String,
) extends UICommonAttribute[EntityType, Long](
      getter,
      setter,
      readConverter,
      writeConverter,
      placeholder,
    ) {

  private val editConverter = Date.epochDayToDate(_, "yyyy-MM-dd")

  def renderEdit(entityVar: Var[Option[EntityType]]): default.Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
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

  override def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(duplicateValuesHandler(attr.getAll.map(x => options.map(o => o.filter(p => p.id == x).map(v => v.name)))))
  }

  def renderEdit(entityVar: Var[Option[EntityType]]): default.Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
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
