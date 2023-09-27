package de.tu_darmstadt.informatik.st.reform.entity

import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.duplicateValuesHandler
import de.tu_darmstadt.informatik.st.reform.given
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils
import de.tu_darmstadt.informatik.st.reform.services.Page
import outwatch.*
import outwatch.dsl.*
import rescala.default
import rescala.default.*

import scalajs.js
import webapp.duplicateValuesHandler
import webapp.given
import webapp.*
import webapp.components.common.*
import webapp.npm.JSUtils
import webapp.services.RoutingService
import webapp.services.Page

class UIFormat[EntityType](val condition: (id: String, entity: EntityType) => Signal[Boolean], val classes: String) {
  def apply(id: String, entity: EntityType): Signal[String] = Signal {
    if (this.condition(id, entity).value) {
      classes
    } else {
      ""
    }
  }
}

abstract class UIBasicAttribute[EntityType](
    val label: String,
    val width: Option[String] = None,
    val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
) {

  def render(id: String, entity: EntityType): VMod = {
    div()
  }

  def renderEdit(formId: String, editing: Var[Option[(EntityType, Var[EntityType])]], props: VMod*): VMod

  def uiFilter: UIFilter[EntityType] = UIFilterNothing()
}

abstract class UIAttribute[EntityType, AttributeType](
    val getter: EntityType => Attribute[AttributeType],
    val readConverter: AttributeType => String,
    override val label: String,
    override val width: Option[String] = None,
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)
    extends UIBasicAttribute[EntityType](label, width, formats) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    div(
      formats.map(f => cls <-- f.apply(id, entity)),
      duplicateValuesHandler(attr.getAll.map(x => readConverter(x))),
    )
  }

  override def renderEdit(
      formId: String,
      editing: Var[Option[(EntityType, Var[EntityType])]],
      props: VMod*,
  ): VMod

  override def uiFilter: UIFilter[EntityType] = UISubstringFilter(this)
}

class UIReadOnlyAttribute[EntityType, T](
    val getter: (String, EntityType) => Signal[T],
    val readConverter: T => String,
    override val label: String,
    override val width: Option[String] = None,
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using renderMagic: Render[T])
    extends UIBasicAttribute[EntityType](label, width, formats) {
  override def render(id: String, entity: EntityType): VMod = {
    div(cls := "px-4 min-h-9 flex items-center", div(getter(id, entity)), formats.map(f => cls <-- f.apply(id, entity)))
  }

  override def renderEdit(formId: String, editing: Var[Option[(EntityType, Var[EntityType])]], props: VMod*): VMod = {
    div()
  }

  override def uiFilter: UIFilter[EntityType] = UIFilterNothing()
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
    override val width: Option[String] = None,
    val regex: String = ".*",
    val stepSize: String = "1",
    val min: String = "",
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)
    extends UIAttribute[EntityType, AttributeType](
      getter = getter,
      readConverter = readConverter,
      label = label,
      width = width,
      formats = formats,
    ) {

  private def set(entityVar: Var[EntityType], x: AttributeType): Unit = {
    entityVar.transform(e => {
      val attr = getter(e)
      setter(e, attr.set(x))
    })
  }

  protected def renderEditInput(
      _formId: String,
      attr: Signal[Attribute[AttributeType]],
      set: AttributeType => Unit,
      datalist: Option[String] = None,
      entity: Var[EntityType],
      props: VMod*,
  ): VMod =
    TableInput(
      // cls := "input valid:input-success bg-gray-50 input-ghost dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white !outline-0 rounded-none w-full border border-gray-300 h-9",
      `type` := fieldType,
      formId := _formId,
      required := isRequired,
      stepAttr := stepSize,
      pattern := regex,
      VMod.attr("min") := min,
      value <-- attr.map(getEditString),
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
      props,
    )

  def renderEdit(
      formId: String,
      editing: Var[Option[(EntityType, Var[EntityType])]],
      props: VMod*,
  ): VMod = Signal.dynamic {
    editing.value.map(editing => {
      val (editStart, entityVar) = editing
      val editStartAttr = getter(editStart)
      val id = s"${js.Math.round(js.Math.random() * 1000000)}"
      div(
        cls := "relative min-w-[1rem] edit-value",
        renderEditInput(
          formId,
          entityVar.map(getter(_)),
          x => set(entityVar, x),
          Some(s"$id-conflicting-values"),
          entityVar,
          props,
        ),
        if (editStartAttr.getAll.size > 1) {
          Some(
            Seq(
              dataList(
                idAttr := s"$id-conflicting-values",
                renderConflicts(editStartAttr),
              ),
            ),
          )
        } else {
          None
        },
      )
    })
  }

  protected def getEditString(attr: Attribute[AttributeType]): String =
    attr.get.map(x => editConverter(x)).getOrElse("")

  private def renderConflicts(attr: Attribute[AttributeType]): VMod =
    attr.getAll.map(x => option(value := readConverter(x)))
}

class UINumberAttribute[EntityType, AttributeType](
    getter: EntityType => Attribute[AttributeType],
    setter: (EntityType, Attribute[AttributeType]) => EntityType,
    readConverter: AttributeType => String,
    editConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    label: String,
    min: String,
    isRequired: Boolean,
    regex: String,
    stepSize: String,
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)(implicit ordering: Ordering[AttributeType])
    extends UITextAttribute[EntityType, AttributeType](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      editConverter = editConverter,
      writeConverter = writeConverter,
      min = min,
      label = label,
      isRequired = isRequired,
      regex = regex,
      width = None,
      stepSize = stepSize,
      fieldType = "number",
      formats = formats,
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
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)
    extends UITextAttribute[EntityType, Long](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      writeConverter = writeConverter,
      editConverter = JSUtils.toYYYYMMDD,
      label = label,
      width = None,
      isRequired = isRequired,
      fieldType = "date",
      formats = formats,
    ) {

  override def renderEditInput(
      _formId: String,
      attr: Signal[Attribute[Long]],
      set: Long => Unit,
      datalist: Option[String] = None,
      entity: Var[EntityType],
      props: VMod*,
  ): VMod = TableInput(
    cls := "input",
    placeholder := "dd.mm.yyyy",
    `type` := "date",
    formId := _formId,
    required := isRequired,
    minAttr := min,
    value <-- attr.map(getEditString),
    onInput.value --> {
      val evt = Evt[String]()
      ignoreDisconnectable(evt.observe(set.compose(writeConverter)))
      evt
    },
    props,
  )

  override def uiFilter: UIFilter[EntityType] = UIIntervalFilter(this)
}

class UICheckboxAttribute[EntityType](
    getter: EntityType => Attribute[Boolean],
    setter: (EntityType, Attribute[Boolean]) => EntityType,
    label: String,
    isRequired: Boolean,
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)
    extends UITextAttribute[EntityType, Boolean](
      getter = getter,
      setter = setter,
      readConverter = _.toString,
      editConverter = _.toString,
      writeConverter = _.toBoolean,
      label = label,
      width = None,
      isRequired = isRequired,
      fieldType = "checkbox",
      formats = formats,
    ) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    div(
      formats.map(f => cls <-- f.apply(id, entity)),
      duplicateValuesHandler(attr.getAll.map(if (_) "Yes" else "No")),
    )
  }

  override def renderEditInput(
      _formId: String,
      attr: Signal[Attribute[Boolean]],
      set: Boolean => Unit,
      datalist: Option[String] = None,
      entity: Var[EntityType],
      props: VMod*,
  ): VMod = Checkbox(
    CheckboxStyle.Default,
    cls := "absolute top-1/2 -translate-y-1/2",
    formId := _formId,
    checked <-- attr.map(_.get.getOrElse(false)),
    onClick.foreach(_ => set(!attr.now.get.getOrElse(false))),
    props,
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
    val options: EntityType => Signal[Seq[SelectOption]],
    val optionsForFilter: Signal[Seq[SelectOption]],
    searchEnabled: Boolean = true,
    createPage: Option[Page] = None,
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)
    extends UITextAttribute[EntityType, AttributeType](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      editConverter = _.toString,
      writeConverter = writeConverter,
      label = label,
      width = Some("200px"),
      isRequired = isRequired,
      fieldType = "select",
      formats = formats,
    ) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    div(
      cls := "!rounded-none",
      formats.map(f => cls <-- f.apply(id, entity)),
      duplicateValuesHandler(
        attr.getAll.map(x => Signal { options(entity).value.filter(p => p.id == x).map(v => v.name) }),
      ),
    )
  }

  override def uiFilter: UIFilter[EntityType] = UISelectFilter(this)

  override def renderEditInput(
      _formId: String,
      attr: Signal[Attribute[AttributeType]],
      set: AttributeType => Unit,
      datalist: Option[String] = None,
      entity: Var[EntityType],
      props: VMod*,
  ): VMod = {
    Select(
      Signal.dynamic { options(entity.value).value },
      v => {
        set(writeConverter(v))
      },
      attr.map(getEditString),
      searchEnabled, {
        createPage match {
          case Some(createPage) =>
            a(
              href := jsImplicits.routing.linkPath(createPage),
              target := "_blank",
              "Nothing found. Click here to create.",
            )
          case None => span("Nothing found...")
        }
      },
      isRequired,
      isRequired,
      formId := _formId,
      props,
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
    val options: EntityType => Signal[Seq[SelectOption]],
    val optionsForFilter: Signal[Seq[SelectOption]],
    showItems: Int = 5,
    searchEnabled: Boolean = true,
    createPage: Option[Page] = None,
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)
    extends UITextAttribute[EntityType, Seq[String]](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      editConverter = _.toString,
      writeConverter = writeConverter,
      label = label,
      width = Some("400px"),
      isRequired = isRequired,
      fieldType = "select",
      formats = formats,
    ) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    div(
      formats.map(f => cls <-- f.apply(id, entity)),
      duplicateValuesHandler(
        Seq(
          div(
            cls := "flex flex-row gap-2 flex-wrap",
            attr.getAll
              .map(x =>
                x.map(id =>
                  Signal {
                    options(entity).value
                      .filter(p => p.id.equals(id))
                      .map(v => div(cls := "bg-slate-300 px-2 py-0.5 rounded-md dark:bg-gray-500", v.name))
                  },
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
      attr: Signal[Attribute[Seq[String]]],
      set: Seq[String] => Unit,
      datalist: Option[String] = None,
      entity: Var[EntityType],
      props: VMod*,
  ): VMod = {
    Seq(
      MultiSelect(
        Signal.dynamic { options(entity.value).value },
        v => {
          set(v)
        },
        attr.map(_.get.getOrElse(Seq())),
        showItems,
        searchEnabled, {
          createPage match {
            case Some(createPage) =>
              a(
                href := jsImplicits.routing.linkPath(createPage),
                target := "_blank",
                "Nothing found. Click here to create.",
              )
            case None => span("Nothing found...")
          }
        },
        isRequired,
        formId := _formId,
        props,
      ),
    )
  }
}

class UICheckboxListAttribute[EntityType](
    getter: EntityType => Attribute[Seq[String]],
    setter: (EntityType, Attribute[Seq[String]]) => EntityType,
    readConverter: Seq[String] => String,
    writeConverter: String => Seq[String],
    label: String,
    isRequired: Boolean,
    val options: EntityType => Signal[Seq[SelectOption]],
    val optionsForFilter: Signal[Seq[SelectOption]],
    override val formats: Seq[UIFormat[EntityType]] = Seq.empty[UIFormat[EntityType]],
)(using jsImplicits: JSImplicits)
    extends UITextAttribute[EntityType, Seq[String]](
      getter = getter,
      setter = setter,
      readConverter = readConverter,
      editConverter = _.toString,
      writeConverter = writeConverter,
      label = label,
      width = Some("350px"),
      isRequired = isRequired,
      fieldType = "select",
      formats = formats,
    ) {

  override def render(id: String, entity: EntityType): VMod = {
    val attr = getter(entity)
    div(
      formats.map(f => cls <-- f.apply(id, entity)),
      duplicateValuesHandler(
        Seq(
          div(
            cls := "flex flex-row gap-2 flex-wrap",
            attr.getAll
              .map(x =>
                x.map(id =>
                  Signal { options(entity).value.filter(p => p.id.equals(id)).map(v => v.name).mkString(", ") },
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
      attr: Signal[Attribute[Seq[String]]],
      set: Seq[String] => Unit,
      datalist: Option[String] = None,
      entity: Var[EntityType],
      props: VMod*,
  ): VMod = {
    Seq(
      CheckboxList(
        Signal.dynamic { options(entity.value).value },
        v => {
          set(v)
        },
        attr.map(_.get.getOrElse(Seq())),
        div("Nothing found..."),
        isRequired,
        formId := _formId,
        props,
      ),
    )

  }
}
