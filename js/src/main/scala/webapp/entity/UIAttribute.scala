package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
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
    val label: String,
    val isRequired: Boolean,
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
    td(cls := "px-6 py-0", duplicateValuesHandler(attr.getAll.map(x => readConverter(x))))
  }

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]): Signal[Option[outwatch.VNode]]
}

case class UIAttribute[EntityType, AttributeType](
    override val getter: EntityType => Attribute[AttributeType],
    override val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    override val readConverter: AttributeType => String,
    override val writeConverter: String => AttributeType,
    override val label: String,
    override val isRequired: Boolean,
    fieldType: String,
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      label,
      isRequired,
    ) {

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]): Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls := "px-6 py-0",
          input(
            cls := "input valid:input-success bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg block w-full p-2.5 dark:bg-gray-700  dark:placeholder-gray-400 dark:text-white",
            `type` := fieldType,
            formId := formAttr, // TODO FIXME check browser support
            required := isRequired,
            value := attr.get.map(x => readConverter(x)).getOrElse(""),
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
            placeholder := label,
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

case class UIDateAttribute[EntityType](
    override val getter: EntityType => Attribute[Long],
    override val setter: (EntityType, Attribute[Long]) => EntityType,
    override val readConverter: Long => String,
    override val writeConverter: String => Long,
    override val label: String,
    override val isRequired: Boolean,
    min: String = "",
) extends UICommonAttribute[EntityType, Long](
      getter,
      setter,
      readConverter,
      writeConverter,
      label,
      isRequired,
    ) {

  private val editConverter = Date.epochDayToDate(_, "yyyy-MM-dd")

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]): Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls := "px-6 py-0",
          input(
            cls := "input valid:input-success",
            `type` := "date",
            formId := formAttr, // TODO FIXME check browser support
            required := isRequired,
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
    override val label: String,
    override val isRequired: Boolean,
    options: Signal[List[UIOption[Signal[String]]]],
) extends UICommonAttribute[EntityType, AttributeType](
      getter,
      setter,
      readConverter,
      writeConverter,
      label,
      isRequired,
    ) {

  override def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(
      cls := "px-6 py-0",
      duplicateValuesHandler(attr.getAll.map(x => options.map(o => o.filter(p => p.id == x).map(v => v.name)))),
    )
  }

  def renderEdit(formAttr: String, entityVar: Var[Option[EntityType]]): Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls := "px-6 py-0",
          select(
            cls := "input valid:input-success",
            formId := formAttr, // TODO FIXME check browser support
            required := isRequired,
            onInput.value --> {
              val evt = Evt[String]()
              evt.observe(x => setFromString(entityVar, x))
              evt
            },
            option(VModifier.attr("value") := "", "Bitte wählen..."),
            options.map(o =>
              o.map(v => option(value := v.id, selected := attr.get.map(x => readConverter(x)).contains(v.id), v.name)),
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
