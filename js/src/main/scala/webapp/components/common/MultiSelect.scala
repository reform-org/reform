package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js
import rescala.default.*
import webapp.npm.JSUtils.createPopper
import webapp.given
import org.scalajs.dom.HTMLInputElement
import webapp.components.Icons
import org.scalajs.dom.document

class SelectOption[NameType](
    val id: String,
    val name: NameType,
) {}

def MultiSelect[T <: VMod](
    options: Signal[List[SelectOption[Signal[String]]]],
    onInput: (value: Seq[String]) => Unit,
    value: Var[Seq[String]],
    showItems: Int = 5,
    props: T*,
): VNode = {
  val id = s"multi-select-${js.Math.round(js.Math.random() * 100000)}"
  val search = Var("")

  createPopper(s"#$id .multiselect-select", s"#$id .multiselect-dropdown-list-wrapper")

  div(
    cls := "multiselect-dropdown dropdown bg-slate-50 border border-slate-200 relative w-full h-9 rounded",
    props,
    idAttr := id,
    div(
      cls := "multiselect-select flex flex-row w-full h-full items-center pl-2",
      div(
        cls := "flex flex-row gap-2",
        options.map(o =>
          value
            .map(s =>
              o.filter(v => s.contains(v.id))
                .slice(0, showItems)
                .map(v =>
                  div(
                    cls := "bg-slate-300 px-2 py-0.5 rounded-md flex flex-row gap-1 items-center",
                    v.name,
                    div(
                      Icons.close("w-4 h-4", "#64748b"),
                      cls := "cursor-pointer",
                      onClick.foreach(_ => {
                        onInput(
                          document
                            .querySelectorAll(s"#$id input[type=checkbox]:checked")
                            .map(element => element.id)
                            .filter(id => id != v.id)
                            .asInstanceOf[Seq[String]],
                        )
                      }),
                    ),
                  ),
                ),
            ),
        ),
        value.map(s => {
          if (s.size > showItems) {
            Some(div(cls := "flex items-center justify-center text-slate-400", s"+${s.size - showItems}"))
          } else None
        }),
        value.map(s => {
          if (s.size == 0) {
            Some(div(cls := "flex items-center justify-center text-slate-400", "Select..."))
          } else None
        }),
      ),
      label(
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
        outwatch.dsl.onInput.value --> search,
        outwatch.dsl.value <-- search,
      ),
      div(
        cls := "p-2 border-b border-slate-200",
        label(
          input(
            tpe := "checkbox",
            cls := "mr-2",
            idAttr := s"all-checkbox-$id",
            onClick.foreach(e => {
              if (e.target.asInstanceOf[HTMLInputElement].checked) {
                onInput(
                  document
                    .querySelectorAll(s"#$id input[type=checkbox]")
                    .map(element => element.id)
                    .asInstanceOf[Seq[String]],
                )
              } else {
                onInput(Seq().asInstanceOf[Seq[String]])
              }

            }),
          ),
          forId := s"all-checkbox-$id",
          cls := "w-full block",
          tabIndex := 0,
          "Select All",
        ),
      ),
      div(
        cls := "multiselect-dropdown-list",
        options.map(option =>
          option.map(uiOption => {
            uiOption.name.map(name => {
              search.map(searchKey => {
                if (searchKey.isBlank() || name.toLowerCase().contains(searchKey.toLowerCase())) {
                  Some(
                    label(
                      cls := "block w-full hover:bg-slate-50 px-2 py-0.5",
                      input(
                        tpe := "checkbox",
                        cls := "mr-2",
                        checked <-- value.map(i => i.contains(uiOption.id)),
                        idAttr := uiOption.id,
                        onClick.foreach(_ => {
                          onInput(
                            document
                              .querySelectorAll(s"#$id input[type=checkbox]:checked")
                              .map(element => element.id)
                              .asInstanceOf[Seq[String]],
                          )
                        }),
                      ),
                      tabIndex := 0,
                      uiOption.name,
                      forId := uiOption.id,
                    ),
                  )
                } else None
              })
            })

          }),
        ),
      ),
    ),
  )
}
