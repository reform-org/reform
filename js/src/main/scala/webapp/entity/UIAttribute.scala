package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.duplicateValuesHandler
import webapp.given
import webapp.utils.Date
import webapp.*

class UIOption[NameType](
    val id: String,
    val name: NameType,
) {}

class UIAttribute[EntityType, AttributeType](
    val getter: EntityType => Attribute[AttributeType],
    val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    val readConverter: AttributeType => String,
    val writeConverter: String => AttributeType,
    val label: String,
    val isRequired: Boolean,
    val fieldType: String = "text",
) {
  private def set(entityVar: Var[Option[EntityType]], x: AttributeType): Unit = {
    entityVar.transform(
      _.map(e => {
        val attr = getter(e)
        setter(e, attr.set(x))
      }),
    )
  }

  def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(cls := "px-6 py-0", duplicateValuesHandler(attr.getAll.map(x => readConverter(x))))
  }

  def renderEdit(formId: String, entityVar: Var[Option[EntityType]]): Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls := "px-6 py-0",
          renderEditInput(formId, attr, x => set(entityVar, x)),
          if (attr.getAll.size > 1) {
            Some(
              p(
                "Conflicting values: ",
                renderConflicts(attr),
              ),
            )
          } else {
            None
          },
        )
      })
    }
  }

  protected def renderEditInput(_formId: String, attr: Attribute[AttributeType], set: AttributeType => Unit): VNode =
    input(
      cls := "input valid:input-success bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg block w-full p-2.5 dark:bg-gray-700  dark:placeholder-gray-400 dark:text-white",
      `type` := fieldType,
      formId := _formId,
      required := isRequired,
      value := attr.get.map(x => readConverter(x)).getOrElse(""),
      onInput.value --> {
        val evt = Evt[String]()
        ignoreDisconnectable(evt.observe(set.compose(writeConverter)))
        evt
      },
      placeholder := label,
    )

  private def renderConflicts(attr: Attribute[AttributeType]): String =
    attr.getAll.map(x => readConverter(x)).mkString("/")
}

class UIDateAttribute[EntityType](
    getter: EntityType => Attribute[Long],
    setter: (EntityType, Attribute[Long]) => EntityType,
    readConverter: Long => String,
    writeConverter: String => Long,
    label: String,
    isRequired: Boolean,
    min: String = "",
) extends UIAttribute[EntityType, Long](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      writeConverter = writeConverter,
      label = label,
      isRequired = isRequired,
      fieldType = "date",
    ) {

  private val editConverter = Date.epochDayToDate(_, "yyyy-MM-dd")

  override def renderEditInput(_formId: String, attr: Attribute[Long], set: Long => Unit): VNode = input(
    cls := "input valid:input-success",
    `type` := "date",
    formId := _formId,
    required := isRequired,
    minAttr := min,
    value := attr.get.map(x => editConverter(x)).getOrElse(""),
    onInput.value --> {
      val evt = Evt[String]()
      ignoreDisconnectable(evt.observe(set.compose(writeConverter)))
      evt
    },
  )
}

class UICheckboxAttribute[EntityType](
    getter: EntityType => Attribute[Boolean],
    setter: (EntityType, Attribute[Boolean]) => EntityType,
    label: String,
    isRequired: Boolean,
) extends UIAttribute[EntityType, Boolean](
      getter = getter,
      setter = setter,
      readConverter = _.toString,
      writeConverter = _.toBoolean,
      label = label,
      isRequired = isRequired,
      fieldType = "checkbox",
    ) {

  override def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(
      cls := "px-6 py-0",
      duplicateValuesHandler(attr.getAll.map(booleanToGerman)),
    )
  }

  private def booleanToGerman(b: Boolean) = {
    if (b) {
      "Ja"
    } else {
      "Nein"
    }
  }

  override def renderEditInput(_formId: String, attr: Attribute[Boolean], set: Boolean => Unit): VNode = input(
    cls := "input valid:input-success",
    `type` := "checkbox",
    formId := _formId,
    checked := attr.get.getOrElse(false),
    onClick.foreach(_ => set(!attr.get.getOrElse(false))),
  )
}

class UISelectAttribute[EntityType, AttributeType](
    getter: EntityType => Attribute[AttributeType],
    setter: (EntityType, Attribute[AttributeType]) => EntityType,
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    label: String,
    isRequired: Boolean,
    options: Signal[List[UIOption[Signal[String]]]],
) extends UIAttribute[EntityType, AttributeType](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      writeConverter = writeConverter,
      label = label,
      isRequired = isRequired,
      fieldType = "select",
    ) {

  override def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(
      cls := "px-6 py-0",
      duplicateValuesHandler(attr.getAll.map(x => options.map(o => o.filter(p => p.id == x).map(v => v.name)))),
    )
  }

  override def renderEditInput(_formId: String, attr: Attribute[AttributeType], set: AttributeType => Unit): VNode =
    select(
      cls := "input valid:input-success",
      formId := _formId,
      required := isRequired,
      onInput.value --> {
        val evt = Evt[String]()
        ignoreDisconnectable(evt.observe(set.compose(writeConverter)))
        evt
      },
      option(VMod.attr("value") := "", "Bitte wählen..."),
      options.map(o =>
        o.map(v => option(value := v.id, selected := attr.get.map(x => readConverter(x)).contains(v.id), v.name)),
      ),
    )
}
