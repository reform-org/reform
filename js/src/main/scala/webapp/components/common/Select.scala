package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js
import rescala.default.*
import webapp.npm.JSUtils.createPopper
import webapp.given
import org.scalajs.dom.HTMLInputElement
import webapp.components.Icons
import org.scalajs.dom.{console, document}

def Select[T <: VMod](
    options: Signal[List[SelectOption[Signal[String]]]],
    onInput: (value: String) => Unit,
    value: Var[String],
    props: T*,
): VNode = {
  val id = s"select-${js.Math.round(js.Math.random() * 100000)}"
  val search = Var("")

  createPopper(s"#$id .select-select", s"#$id .select-dropdown-list-wrapper")

  div(
    cls := "select-dropdown dropdown bg-slate-50 border border-slate-200 relative w-full h-9 rounded",
    props,
    idAttr := id,
    div(
      cls := "select-select flex flex-row w-full h-full items-center pl-2",
      options.map(o =>
        value
          .map(s => {
            val selectedOption = o.find(v => s.contains(v.id))
            selectedOption match {
              case None    => div()
              case Some(v) => div(v.name)
            }
          }),
      ),
      value.map(s => {
        if (s.size == 0) {
          Some(div(cls := "flex items-center justify-center text-slate-400", "Select..."))
        } else None
      }),
      label(
        tabIndex := 0,
        cls := "grow relative pr-7 h-full",
        div(cls := "absolute right-2 top-1/2 -translate-y-1/2", Icons.notch("w-4 h-4")),
      ),
    ),
    div(
      cls := "select-dropdown-list-wrapper z-100 bg-white dropdown-content shadow-lg w-full rounded top-0 left-0 border border-slate-200",
      input(
        cls := "select-dropdown-search p-2 w-full focus:outline-0 border-b border-slate-200",
        placeholder := "Search Options...",
        outwatch.dsl.onInput.value --> search,
        outwatch.dsl.value <-- search,
      ),
      div(
        cls := "select-dropdown-list",
        options.map(option =>
          option.map(uiOption => {
            uiOption.name.map(name => {
              search.map(searchKey => {
                if (searchKey.isBlank() || name.toLowerCase().contains(searchKey.toLowerCase())) {
                  Some(
                    label(
                      cls := "block w-full hover:bg-slate-50",
                      input(
                        tpe := "radio",
                        cls := "hidden peer",
                        checked <-- value.map(i => i.contains(uiOption.id)),
                        idAttr := uiOption.id,
                        VMod.attr("name") := id,
                        onClick.foreach(_ => {
                          onInput(
                            document
                              .querySelector(s"#$id input[type=radio]:checked")
                              .id,
                          )
                        }),
                      ),
                      tabIndex := 0,
                      div(
                        cls := "peer-checked:bg-blue-400 peer-checked:text-white px-2 py-0.5",
                        uiOption.name,
                      ),
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
