package webapp.entity

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.duplicateValuesHandler
import webapp.given
import webapp.*
import webapp.utils.Date
import scala.scalajs.js
import org.scalajs.dom.document
import org.scalajs.dom.HTMLInputElement
import webapp.components.Icons

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
      stepAttr := stepSize,
      pattern := regex,
      value := getEditString(attr),
      onInput.value --> {
        val evt = Evt[String]()
        ignoreDisconnectable(evt.observe(set.compose(writeConverter)))
        evt
      },
      placeholder := label,
    )

  protected def getEditString(attr: Attribute[AttributeType]): String =
    attr.get.map(x => editConverter(x)).getOrElse("")

  private def renderConflicts(attr: Attribute[AttributeType]): String =
    attr.getAll.map(x => readConverter(x)).mkString("/")

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

  override def renderEditInput(_formId: String, attr: Attribute[Long], set: Long => Unit): VNode = input(
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
      cls := "px-6 py-0",
      duplicateValuesHandler(attr.getAll.map(if (_) "Yes" else "No")),
    )
  }

  override def renderEditInput(_formId: String, attr: Attribute[Boolean], set: Boolean => Unit): VNode = input(
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

class UIMultiSelectAttribute[EntityType, AttributeType <: Seq[?]](
    getter: EntityType => Attribute[AttributeType],
    setter: (EntityType, Attribute[AttributeType]) => EntityType,
    readConverter: AttributeType => String,
    writeConverter: String => AttributeType,
    label: String,
    isRequired: Boolean,
    options: Signal[List[UIOption[Signal[String]]]],
    showItems: Int = 5,
    placeholderText: String = "Select...",
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
      cls := "px-6 py-0",
      duplicateValuesHandler(
        Seq(
          div(
            cls := "flex flex-row gap-2",
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

  override def renderEditInput(_formId: String, attr: Attribute[AttributeType], set: AttributeType => Unit): VNode = {
    val id = s"multi-select-${js.Math.round(js.Math.random() * 100000)}"
    val search = Var("")
    div(
      cls := "multiselect-dropdown dropdown bg-slate-50 border border-slate-200 relative w-full h-9 rounded",
      idAttr := id,
      div(
        cls := "flex flex-row w-full h-full items-center pl-2",
        div(
          cls := "flex flex-row gap-2",
          options.map(o =>
            attr.getAll
              .map(s =>
                o.filter(v => s.contains(v.id))
                  .slice(0, 4)
                  .map(v =>
                    div(
                      cls := "bg-slate-300 px-2 py-0.5 rounded-md flex flex-row gap-1 items-center",
                      v.name,
                      div(
                        Icons.close("w-4 h-4", "#64748b"),
                        cls := "cursor-pointer",
                        onClick.foreach(_ => {
                          set(
                            document
                              .querySelectorAll(s"#$id input[type=checkbox]:checked")
                              .map(element => element.id)
                              .filter(id => id != v.id)
                              .asInstanceOf[AttributeType],
                          )
                        }),
                      ),
                    ),
                  ),
              ),
          ),
          if (attr.getAll(0).size > showItems) {
            Some(div(cls := "flex items-center justify-center text-slate-400", s"+${attr.getAll(0).size - showItems}"))
          } else None,
          if (attr.getAll(0).size == 0) {
            Some(div(cls := "flex items-center justify-center text-slate-400", placeholderText))
          } else None,
        ),
        outwatch.dsl.label(
          tabIndex := 0,
          cls := "grow relative pr-7 h-full",
          div(cls := "absolute right-2 top-1/2 -translate-y-1/2", Icons.notch("w-4 h-4")),
        ),
      ),
      div(
        cls := "multiselect-dropdown-list-wrapper z-100 bg-white dropdown-content shadow-lg w-full rounded top-0 left-0 border border-slate-200",
        input(
          cls := "multiselect-dropdown-search p-2 w-full focus:outline-0 border-b border-slate-200",
          placeholder := "Search Options...",
          onInput.value --> search,
          value <-- search,
        ),
        div(
          cls := "p-2 border-b border-slate-200",
          input(
            tpe := "checkbox",
            cls := "mr-2",
            idAttr := s"all-checkbox-$id",
            onClick.foreach(e => {
              if (e.target.asInstanceOf[HTMLInputElement].checked) {
                set(
                  document
                    .querySelectorAll(s"#$id input[type=checkbox]")
                    .map(element => element.id)
                    .asInstanceOf[AttributeType],
                )
              } else {
                set(Seq().asInstanceOf[AttributeType])
              }

            }),
          ),
          outwatch.dsl.label(
            forId := s"all-checkbox-$id",
            tabIndex := 0,
            "Select All",
          ),
        ),
        div(
          cls := "multiselect-dropdown-list p-2",
          options.map(option =>
            attr.getAll.map(attribute =>
              option.map(uiOption => {
                uiOption.name.map(name => {
                  search.map(searchKey => {
                    if (searchKey.isBlank() || name.toLowerCase().contains(searchKey.toLowerCase())) {
                      Some(
                        div(
                          input(
                            tpe := "checkbox",
                            cls := "mr-2",
                            checked := attribute.contains(uiOption.id),
                            idAttr := uiOption.id,
                            onClick.foreach(_ => {
                              set(
                                document
                                  .querySelectorAll(s"#$id input[type=checkbox]:checked")
                                  .map(element => element.id)
                                  .asInstanceOf[AttributeType],
                              )
                            }),
                          ),
                          outwatch.dsl.label(
                            tabIndex := 0,
                            uiOption.name,
                            forId := uiOption.id,
                          ),
                        ),
                      )
                    } else None
                  })
                })

              }),
            ),
          ),
        ),
      ),
      formId := _formId,
      required := isRequired,
    )
  }
}
