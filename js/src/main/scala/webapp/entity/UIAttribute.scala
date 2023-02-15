package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.duplicateValuesHandler
import webapp.given
import webapp.*
import webapp.utils.Date

class UIOption[NameType](
    val id: String,
    val name: NameType,
) {}

class UIAttribute[EntityType, AttributeType](
    val getter: EntityType => Attribute[AttributeType],
    val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    val readConverter: AttributeType => String,
    val editConverter: AttributeType => String,
    val writeConverter: String => AttributeType,
    val label: String,
    val isRequired: Boolean,
    val fieldType: String,
    val regex: String = ".*",
    val stepSize: String = "1",
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
    td(
      cls := "border border-gray-300 p-0",
      duplicateValuesHandler(attr.getAll.map(x => readConverter(x))),
    )
  }

  def renderEdit(formId: String, entityVar: Var[Option[EntityType]]): Signal[Option[VNode]] = {
    entityVar.map {
      _.map(entity => {
        val attr = getter(entity)
        td(
          cls := " border-0 px-0 py-0",
          renderEditInput(formId, attr, x => set(entityVar, x), Some("conflicting-values")),
          if (attr.getAll.size > 1) {
            Some(
              Seq(
                dataList(
                  idAttr := "conflicting-values",
                  renderConflicts(attr),
                ),
              ),
            )
          } else {
            None
          },
        )
      })
    }
  }

  protected def renderEditInput(
      _formId: String,
      attr: Attribute[AttributeType],
      set: AttributeType => Unit,
      datalist: Option[String] = None,
  ): VNode =
    input(
      cls := "input valid:input-success bg-gray-50 input-ghost dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white !outline-0 rounded-none w-full border border-gray-300 h-9",
      `type` := fieldType,
      formId := _formId,
      required := isRequired,
      stepAttr := stepSize,
      pattern := regex,
      value := getEditString(attr),
      onInput.value --> {
        val evt = Evt[String]()
        ignoreDisconnectable(evt.observe(set.compose(writeConverter)))
        evt
      },
      placeholder := label, {
        datalist match {
          case None        => {}
          case Some(value) => listId := value
        }
      },
    )

  protected def getEditString(attr: Attribute[AttributeType]): String =
    attr.get.map(x => editConverter(x)).getOrElse("")

  private def renderConflicts(attr: Attribute[AttributeType]): Seq[VNode] =
    attr.getAll.map(x => option(value := readConverter(x)))

  def uiFilter: UIFilter[EntityType] = UISubstringFilter(this)
}

class UINumberAttribute[EntityType, AttributeType](
    getter: EntityType => Attribute[AttributeType],
    setter: (EntityType, Attribute[AttributeType]) => EntityType,
    readConverter: AttributeType => String,
    editConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    label: String,
    isRequired: Boolean,
    regex: String,
    stepSize: String,
)(implicit ordering: Ordering[AttributeType])
    extends UIAttribute[EntityType, AttributeType](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      editConverter = editConverter,
      writeConverter = writeConverter,
      label = label,
      isRequired = isRequired,
      regex = regex,
      stepSize = stepSize,
      fieldType = "number",
    ) {

  override def uiFilter: UIFilter[EntityType] = UIIntervalFilter(this)
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
      editConverter = Date.epochDayToDate(_, "yyyy-MM-dd"),
      label = label,
      isRequired = isRequired,
      fieldType = "date",
    ) {

  override def renderEditInput(
      _formId: String,
      attr: Attribute[Long],
      set: Long => Unit,
      datalist: Option[String] = None,
  ): VNode = input(
    cls := "input valid:input-success",
    `type` := "date",
    formId := _formId,
    required := isRequired,
    minAttr := min,
    value := getEditString(attr),
    onInput.value --> {
      val evt = Evt[String]()
      ignoreDisconnectable(evt.observe(set.compose(writeConverter)))
      evt
    },
  )

  override def uiFilter: UIFilter[EntityType] = UIIntervalFilter(this)
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
      editConverter = _.toString,
      writeConverter = _.toBoolean,
      label = label,
      isRequired = isRequired,
      fieldType = "checkbox",
    ) {

  override def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(
      cls := "border border-gray-300 px-6 py-0",
      duplicateValuesHandler(attr.getAll.map(if (_) "Yes" else "No")),
    )
  }

  override def renderEditInput(
      _formId: String,
      attr: Attribute[Boolean],
      set: Boolean => Unit,
      datalist: Option[String] = None,
  ): VNode = input(
    cls := "input valid:input-success",
    `type` := "checkbox",
    formId := _formId,
    checked := attr.get.getOrElse(false),
    onClick.foreach(_ => set(!attr.get.getOrElse(false))),
  )

  override def uiFilter: UIFilter[EntityType] = UIBooleanFilter(this)
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
      editConverter = _.toString,
      writeConverter = writeConverter,
      label = label,
      isRequired = isRequired,
      fieldType = "select",
    ) {

  override def render(entity: EntityType): VNode = {
    val attr = getter(entity)
    td(
      cls := "border border-gray-300 px-6 py-0",
      duplicateValuesHandler(attr.getAll.map(x => options.map(o => o.filter(p => p.id == x).map(v => v.name)))),
    )
  }

  override def renderEditInput(
      _formId: String,
      attr: Attribute[AttributeType],
      set: AttributeType => Unit,
      datalist: Option[String] = None,
  ): VNode =
    select(
      cls := "input valid:input-success input-ghost",
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
