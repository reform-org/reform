package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default
import rescala.default.*
import webapp.duplicateValuesHandler
import webapp.given
import webapp.*
import webapp.utils.Date
import webapp.components.common.*

abstract class UIBasicAttribute[EntityType](
    val label: String,
) {

  def render(id: String, entity: EntityType): VMod = {
    td(cls := "border border-gray-300 px-4 min-w-[200px]")
  }

  def renderEdit(formId: String, editing: Var[Option[(EntityType, Var[EntityType])]]): VMod

  def uiFilter: UIFilter[EntityType] = UIFilterNothing()
}

abstract class UIAttribute[EntityType, AttributeType](
    val getter: EntityType => Attribute[AttributeType],
    val readConverter: AttributeType => String,
    override val label: String,
) extends UIBasicAttribute[EntityType](label) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    td(
      cls := "border border-gray-300 p-0 min-w-[200px]",
      duplicateValuesHandler(attr.getAll.map(x => readConverter(x))),
    )
  }

  override def renderEdit(
      formId: String,
      editing: Var[Option[(EntityType, Var[EntityType])]],
  ): VMod

  override def uiFilter: UIFilter[EntityType] = UISubstringFilter(this)
}

class UITextAttribute[EntityType, AttributeType](
    override val getter: EntityType => Attribute[AttributeType],
    val setter: (EntityType, Attribute[AttributeType]) => EntityType,
    override val readConverter: AttributeType => String,
    val editConverter: AttributeType => String,
    val writeConverter: String => AttributeType,
    override val label: String,
    val isRequired: Boolean,
    val fieldType: String,
    val regex: String = ".*",
    val stepSize: String = "1",
) extends UIAttribute[EntityType, AttributeType](getter = getter, readConverter = readConverter, label = label) {

  private def set(entityVar: Var[EntityType], x: AttributeType): Unit = {
    entityVar.transform(e => {
      val attr = getter(e)
      setter(e, attr.set(x))
    })
  }

  protected def renderEditInput(
      _formId: String,
      attr: Attribute[AttributeType],
      set: AttributeType => Unit,
      datalist: Option[String] = None,
  ): VMod =
    TableInput(
      // cls := "input valid:input-success bg-gray-50 input-ghost dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white !outline-0 rounded-none w-full border border-gray-300 h-9",
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
          case None        =>
          case Some(value) => listId := value
        }
      },
    )

  def renderEdit(
      formId: String,
      editing: Var[Option[(EntityType, Var[EntityType])]],
  ): VMod = {
    editing.map(
      _.map(editing => {
        val (editStart, entityVar) = editing
        entityVar.map(entity => {
          val attr = getter(entity)
          val editStartAttr = getter(editStart)
          td(
            cls := "border-0 px-0 py-0 min-w-[200px]",
            renderEditInput(formId, attr, x => set(entityVar, x), Some(s"$formId-conflicting-values")),
            if (editStartAttr.getAll.size > 1) {
              Some(
                Seq(
                  dataList(
                    idAttr := s"$formId-conflicting-values",
                    renderConflicts(editStartAttr),
                  ),
                ),
              )
            } else {
              None
            },
          )
        })
      }),
    )
  }

  protected def getEditString(attr: Attribute[AttributeType]): String =
    attr.get.map(x => editConverter(x)).getOrElse("")

  protected def renderConflicts(attr: Attribute[AttributeType]): VMod =
    attr.getAll.map(x => option(value := readConverter(x)))
}

class UIReadOnlyAttribute[EntityType, AttributeType](
    getter: EntityType => Attribute[AttributeType],
    readConverter: AttributeType => String,
    label: String,
) extends UIAttribute[EntityType, AttributeType](getter = getter, readConverter = readConverter, label = label) {

  override def renderEdit(
      formId: String,
      editing: Var[Option[(EntityType, Var[EntityType])]],
  ): VMod = {
    editing.map(_.map(editing => {
      val (startEditEntity, entityVar) = editing
      entityVar.map(entity => getter(entity).get.map(a => td(readConverter(a))))
    }))
  }
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
    extends UITextAttribute[EntityType, AttributeType](
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
) extends UITextAttribute[EntityType, Long](
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
  ): VMod = TableInput(
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
) extends UITextAttribute[EntityType, Boolean](
      getter = getter,
      setter = setter,
      readConverter = _.toString,
      editConverter = _.toString,
      writeConverter = _.toBoolean,
      label = label,
      isRequired = isRequired,
      fieldType = "checkbox",
    ) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    td(
      cls := "border border-gray-300 px-4 py-0",
      duplicateValuesHandler(attr.getAll.map(if (_) "Yes" else "No")),
    )
  }

  override def renderEditInput(
      _formId: String,
      attr: Attribute[Boolean],
      set: Boolean => Unit,
      datalist: Option[String] = None,
  ): VMod = Checkbox(
    CheckboxStyle.Default,
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
    val options: Signal[Seq[SelectOption]],
    searchEnabled: Boolean = true,
) extends UITextAttribute[EntityType, AttributeType](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      editConverter = _.toString,
      writeConverter = writeConverter,
      label = label,
      isRequired = isRequired,
      fieldType = "select",
    ) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    td(
      cls := "border border-gray-300 px-4 py-0 min-w-[200px]",
      duplicateValuesHandler(attr.getAll.map(x => options.map(o => o.filter(p => p.id == x).map(v => v.name)))),
    )
  }

  override def uiFilter: UIFilter[EntityType] = UISelectFilter(this)

  override def renderEditInput(
      _formId: String,
      attr: Attribute[AttributeType],
      set: AttributeType => Unit,
      datalist: Option[String] = None,
  ): VMod = {
    val value = Var(attr.get.getOrElse("").asInstanceOf[String])
    Select(
      options,
      v => {
        value.set(v.asInstanceOf[String])
        set(v.asInstanceOf[AttributeType])
      },
      value,
      searchEnabled,
      span("Nothing found..."),
      formId := _formId,
      required := isRequired,
    )
  }
}

class UIMultiSelectAttribute[EntityType](
    getter: EntityType => Attribute[Seq[String]],
    setter: (EntityType, Attribute[Seq[String]]) => EntityType,
    readConverter: Seq[String] => String,
    writeConverter: String => Seq[String],
    label: String,
    isRequired: Boolean,
    val options: Signal[Seq[MultiSelectOption]],
    showItems: Int = 5,
    searchEnabled: Boolean = true,
) extends UITextAttribute[EntityType, Seq[String]](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      editConverter = _.toString,
      writeConverter = writeConverter,
      label = label,
      isRequired = isRequired,
      fieldType = "select",
    ) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    td(
      cls := "border border-gray-300 p-0 min-w-[350px] max-w-[350px]",
      duplicateValuesHandler(
        Seq(
          div(
            cls := "flex flex-row gap-2 flex-wrap",
            attr.getAll
              .map(x =>
                x.map(id =>
                  options.map(o =>
                    o.filter(p => p.id.equals(id)).map(v => div(cls := "bg-slate-300 px-2 py-0.5 rounded-md", v.name)),
                  ),
                ),
              ),
          ),
        ),
      ),
    )
  }

  override def uiFilter: UIFilter[EntityType] = UIMultiSelectFilter(this)

  override def renderEditInput(
      _formId: String,
      attr: Attribute[Seq[String]],
      set: Seq[String] => Unit,
      datalist: Option[String] = None,
  ): VMod = {
    val value = Var(attr.getAll.head)
    Seq(
      MultiSelect(
        options,
        v => {
          value.set(v.asInstanceOf[Seq[String]])
          set(v)
        },
        value,
        showItems,
        searchEnabled,
        span("Nothing found..."),
        formId := _formId,
        required := isRequired,
      ),
      cls := "min-w-[350px] max-w-[350px]",
    )

  }
}
