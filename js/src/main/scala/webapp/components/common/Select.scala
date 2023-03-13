package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js
import rescala.default.*
import webapp.npm.JSUtils.createPopper
import webapp.given
import webapp.components.icons
import org.scalajs.dom.{console, document}
import org.scalajs.dom.HTMLElement

def Select(
    options: Signal[Seq[SelectOption]],
    onInput: (value: String) => Unit,
    value: Signal[String],
    searchEnabled: Boolean = true,
    emptyState: VMod = span("Nothing found..."),
    required: Boolean = false,
    styleValidity: Boolean = false,
    props: VMod*,
): VNode = {
  val dropdownOpen = Var(false)

  val id = s"select-${js.Math.round(js.Math.random() * 100000)}"
  val search = Var("")

  createPopper(s"#$id .select-select", s"#$id .select-dropdown-list-wrapper")

  def close() = {
    document.querySelector(s"#$id .select-select").asInstanceOf[HTMLElement].click()
  }

  div(
    cls := "rounded select-dropdown dropdown bg-slate-50 border border-gray-300 relative w-full h-9 dark:bg-gray-700 dark:border-none",
    cls <-- dropdownOpen.map(if (_) Some("dropdown-open") else None),
    props,
    idAttr := id,
    div(
      cls := "select-select flex flex-row w-full h-full items-center",
      onClick.foreach(e => {
        dropdownOpen.transform(!_)
      }),
      Signal {
        input(
          outwatch.dsl.value := value.value,
          tpe := "text",
          outwatch.dsl.required := required,
          cls := "peer/select w-[1px] focus:outline-none opacity-0 border-none max-w-[1px] pointer-events-none	",
          tabIndex := -1,
          formId := props
            .collectFirst(p => {
              p match {
                case AccumAttr("form", value, _) => value
                case BasicAttr("form", value)    => value
              }
            })
            .getOrElse("")
            .toString,
        )
      },
      div(
        cls := "flex flex-row w-full h-full items-center pl-2 text-slate-600",
        if (styleValidity)
          cls := "peer-invalid/select:bg-yellow-100 peer-invalid/select:text-yellow-600 peer-invalid/select:border-yellow-600"
        else None,
        Signal {
          if (value.value.isEmpty) {
            Some(
              div(
                if (!styleValidity)
                  cls := "text-slate-400"
                else None,
                cls := "flex items-center justify-center",
                "Select...",
              ),
            )
          } else None
        },
        Signal {
          options.value.find(v => value.value == v.id) match {
            case None    => div()
            case Some(v) => div(v.name)
          }
        },
        label(
          tabIndex := 0,
          cls := "grow relative pr-7 h-full",
          div(cls := "absolute right-2 top-1/2 -translate-y-1/2", icons.Notch(cls := "w-4 h-4")),
        ),
      ),
    ),
    div(
      cls := "select-dropdown-list-wrapper dark:bg-gray-800 dark:border-gray-700 bg-white dropdown-content !transition-none shadow-xl w-full rounded top-0 left-0 border border-gray-300 !z-[100]",
      if (searchEnabled) {
        Some(
          input(
            cls := "select-dropdown-search p-2 w-full focus:outline-0 border-b border-gray-300 dark:bg-gray-700 dark:border-gray-600",
            placeholder := "Search Options...",
            outwatch.dsl.onInput.value --> search,
            outwatch.dsl.value <-- search,
          ),
        )
      } else None,
      div(
        cls := "select-dropdown-list max-h-96 md:max-h-44 sm:max-h-44 overflow-y-auto custom-scrollbar",
        Signal {
          options.value.map(uiOption =>
            Signal {
              if (search.value.isBlank() || uiOption.name.value.toLowerCase().nn.contains(search.value.toLowerCase())) {
                Some(
                  label(
                    cls := "block w-full hover:bg-slate-50 dark:hover:bg-gray-700",
                    input(
                      tpe := "radio",
                      cls := "hidden peer",
                      checked <-- value.map(i => i.contains(uiOption.id)),
                      idAttr := s"$id-${uiOption.id}",
                      VMod.attr("data-id") := uiOption.id,
                      VMod.attr("name") := id,
                      onClick.foreach(_ => {
                        onInput(
                          document
                            .querySelector(s"#$id input[type=radio]:checked")
                            .asInstanceOf[HTMLElement]
                            .dataset
                            .get("id")
                            .getOrElse(""),
                        )
                        close()
                      }),
                    ),
                    tabIndex := 0,
                    div(
                      cls := "peer-checked:bg-blue-400 peer-checked:text-white px-2 py-0.5",
                      uiOption.render,
                    ),
                    forId := s"$id-${uiOption.id}",
                  ),
                )
              } else None
            },
          )
        },
        Signal {
          if (
            Signal(
              options.value
                .map(uiOption =>
                  Signal {
                    search.value.isBlank || uiOption.name.value.toLowerCase().nn.contains(search.value.toLowerCase())
                  },
                ),
            ).flatten.map(_.count(identity) == 0).value
          ) {
            Some(
              div(
                cls := "p-2 flex items-center justify-center text-slate-500 text-sm",
                emptyState,
              ),
            )
          } else {
            None
          }
        },
      ),
    ),
  )
}
