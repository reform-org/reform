package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js
import rescala.default.*
import webapp.npm.JSUtils.createPopper
import webapp.given
import org.scalajs.dom.HTMLInputElement
import webapp.components.icons
import org.scalajs.dom.{console, document, window}
import org.scalajs.dom.HTMLElement
import scala.annotation.nowarn
import org.scalajs.dom.ResizeObserver
import webapp.remToPx

class MultiSelectOption(
    val id: String,
    val name: Signal[String],
) {
  def render: VNode = {
    span(name)
  }
}

def MultiSelect(
    options: Signal[Seq[MultiSelectOption]],
    onInput: (value: Seq[String]) => Unit,
    value: Signal[Seq[String]],
    showItems: Int = 5,
    searchEnabled: Boolean = true,
    emptyState: VMod = span("Nothing found..."),
    props: VMod*,
): VNode = {
  var visibleItems = Var(showItems)
  val dropdownOpen = Var(false)

  val id = s"multi-select-${js.Math.round(js.Math.random() * 100000)}"
  val search = Var("")

  createPopper(s"#$id .multiselect-select", s"#$id .multiselect-dropdown-list-wrapper")

  def updateSelectAll(value: Seq[String]): Unit = {
    val selectAll = Option(
      document
        .querySelector(s"#$id input[type=checkbox]#all-checkbox-$id")
        .asInstanceOf[HTMLInputElement],
    )

    options.map(options => {
      selectAll.map(selectAll => {
        val uncheckedOptions = options.size - value.size

        if (uncheckedOptions == 0) {
          selectAll.checked = true
          selectAll.indeterminate = false
        } else if (uncheckedOptions == options.size) {
          selectAll.checked = false
          selectAll.indeterminate = false
        } else {
          selectAll.indeterminate = true
        }
      })
    }): @nowarn

  }

  val resizeObserver = ResizeObserver((entries, _) => {
    val maxWidth = document.querySelector(s"#$id").getBoundingClientRect().width - remToPx(2.25)
    entries.foreach(entry => {
      if (entry.contentRect.width > maxWidth) {
        visibleItems.transform(_ - 1)
      }
    })
  })

  value.observe(v => updateSelectAll(v)): @nowarn

  div(
    onDomMount.foreach(element => resizeObserver.observe(element.querySelector(".multiselect-value-wrapper"))),
    onDomUnmount.foreach(element => resizeObserver.disconnect()),
    cls := "rounded multiselect-dropdown dropdown bg-slate-50 border border-gray-300 relative w-full h-9 dark:bg-gray-700 dark:border-none",
    cls <-- dropdownOpen.map(if (_) Some("dropdown-open") else None),
    props,
    idAttr := id,
    div(
      cls := "multiselect-select flex flex-row w-full h-full items-center pl-2",
      onClick.foreach(e => {
        dropdownOpen.transform(!_)
      }),
      div(
        cls := "flex flex-row gap-2 multiselect-value-wrapper",
        options.map(o =>
          value
            .map(s =>
              visibleItems.map(visibleItems =>
                o.filter(v => s.contains(v.id))
                  .slice(0, visibleItems)
                  .map(v =>
                    div(
                      cls := "bg-slate-300 px-2 py-0.5 rounded-md flex flex-row gap-1 items-center whitespace-nowrap dark:bg-gray-500",
                      v.name,
                      div(
                        icons.Close(cls := "w-4 h-4 text-slate-600 dark:text-slate-200"),
                        cls := "cursor-pointer",
                        onClick.foreach(_ => {
                          onInput(
                            document
                              .querySelectorAll(s"#$id input[type=checkbox]:not(#all-checkbox-$id):checked")
                              .map(element =>
                                element
                                  .asInstanceOf[HTMLElement]
                                  .dataset
                                  .get("id")
                                  .getOrElse(""),
                              )
                              .filter(id => id != v.id)
                              .asInstanceOf[Seq[String]],
                          )
                        }),
                      ),
                    ),
                  ),
              ),
            ),
        ),
        value.map(s => {
          visibleItems.map(visibleItems => {
            if (s.size > visibleItems) {
              Some(
                div(
                  cls := "flex items-center justify-center text-slate-400 dark:text-gray-200",
                  s"+${s.size - visibleItems}",
                ),
              )
            } else None
          })
        }),
        value.map(s => {
          if (s.size == 0) {
            Some(
              div(
                cls := "flex items-center justify-center text-slate-400 dark:text-gray-200",
                "Select...",
              ),
            )
          } else None
        }),
      ),
      label(
        tabIndex := 0,
        cls := "grow relative pr-7 h-full",
        div(cls := "absolute right-2 top-1/2 -translate-y-1/2", icons.Notch(cls := "w-4 h-4")),
      ),
    ),
    div(
      cls := "multiselect-dropdown-list-wrapper dark:bg-gray-700 dark:border-gray-700 bg-white dropdown-content !transition-none shadow-lg w-full rounded top-0 left-0 border border-gray-300 !z-[100]",
      if (searchEnabled) {
        Some(
          input(
            cls := "multiselect-dropdown-search p-2 w-full focus:outline-0 border-b border-gray-300 dark:bg-gray-700 dark:border-gray-600",
            placeholder := "Search Options...",
            outwatch.dsl.onInput.value --> search,
            outwatch.dsl.value <-- search,
          ),
        )
      } else None,
      div(
        cls := "p-2 border-b border-gray-300 dark:border-gray-600",
        label(
          Checkbox(
            CheckboxStyle.Default,
            cls := "mr-2",
            idAttr := s"all-checkbox-$id",
            onClick.foreach(e => {
              if (e.target.asInstanceOf[HTMLInputElement].checked) {
                onInput(
                  document
                    .querySelectorAll(s"#$id input[type=checkbox]:not(#all-checkbox-$id)")
                    .map(element =>
                      element
                        .asInstanceOf[HTMLElement]
                        .dataset
                        .get("id")
                        .getOrElse(""),
                    )
                    .asInstanceOf[Seq[String]],
                )
              } else {
                onInput(Seq().asInstanceOf[Seq[String]])
              }

            }),
          ),
          forId := s"all-checkbox-$id",
          cls := "w-full block flex items-center",
          tabIndex := 0,
          "Select All",
        ),
      ),
      div(
        cls := "multiselect-dropdown-list max-h-96 md:max-h-44 sm:max-h-44 overflow-y-auto custom-scrollbar",
        options.map(option =>
          option.map(uiOption => {
            uiOption.name.map(name => {
              search.map(searchKey => {
                if (searchKey.isBlank() || name.toLowerCase().nn.contains(searchKey.toLowerCase())) {
                  Some(
                    label(
                      cls := "block w-full hover:bg-slate-50 px-2 py-0.5 flex items-center dark:hover:bg-gray-700",
                      Checkbox(
                        CheckboxStyle.Default,
                        cls := "mr-2",
                        checked <-- value.map(i => i.contains(uiOption.id)),
                        idAttr := s"$id-${uiOption.id}",
                        VMod.attr("data-id") := uiOption.id,
                        onClick.foreach(_ => {
                          onInput(
                            document
                              .querySelectorAll(s"#$id input[type=checkbox]:not(#all-checkbox-$id):checked")
                              .map(element =>
                                element
                                  .asInstanceOf[HTMLElement]
                                  .dataset
                                  .get("id")
                                  .getOrElse(""),
                              )
                              .asInstanceOf[Seq[String]],
                          )
                        }),
                      ),
                      tabIndex := 0,
                      uiOption.render,
                      forId := s"$id-${uiOption.id}",
                    ),
                  )
                } else None
              })
            })

          }),
        ), {
          val noResults = options
            .map(option => {
              Signal(
                option
                  .map(uiOption => {
                    uiOption.name
                      .map(name => {
                        search
                          .map(searchKey =>
                            (searchKey.isBlank() || name.toLowerCase().nn.contains(searchKey.toLowerCase())),
                          )
                      })
                      .flatten
                  }),
              ).flatten.map(options => {
                options.count(identity) == 0
              })
            })
            .flatten
          noResults.map(noResults => {
            if (noResults) {
              Some(
                div(
                  cls := "p-2 flex items-center justify-center text-slate-500 text-sm",
                  emptyState,
                ),
              )
            } else {
              None
            }
          })
        },
      ),
    ),
  )
}
