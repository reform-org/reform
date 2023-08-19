package de.tu_darmstadt.informatik.st.reform.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js
import rescala.default.*
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.createPopper
import de.tu_darmstadt.informatik.st.reform.given
import org.scalajs.dom.HTMLInputElement
import de.tu_darmstadt.informatik.st.reform.components.icons
import org.scalajs.dom.{console, document, window}
import org.scalajs.dom.HTMLElement
import scala.annotation.nowarn
import org.scalajs.dom.ResizeObserver
import de.tu_darmstadt.informatik.st.reform.remToPx
import de.tu_darmstadt.informatik.st.reform.npm.JSUtils.cleanPopper

private class MultiSelect(
    options: Signal[Seq[SelectOption]],
    onInput: Seq[String] => Unit,
    value: Signal[Seq[String]],
    showItems: Int = 5,
    searchEnabled: Boolean = true,
    emptyState: VMod = span("Nothing found..."),
    required: Boolean = false,
    styleValidity: Boolean = false,
    props: VMod*,
) {

  private val visibleItems = Var(showItems)
  private val dropdownOpen = Var(false)

  private val id = s"multi-select-${js.Math.round(js.Math.random() * 100000)}"
  private val search = Var("")

  private def updateSelectAll(value: Seq[String]): Unit = {
    val selectAll = Option(
      document
        .querySelector(s"#$id input[type=checkbox]#all-checkbox-$id")
        .asInstanceOf[HTMLInputElement],
    )

    Signal {
      selectAll.map(selectAll => {
        val uncheckedOptions = options.value.size - value.size

        if (uncheckedOptions == 0) {
          selectAll.checked = true
          selectAll.indeterminate = false
        } else if (uncheckedOptions == options.value.size) {
          selectAll.checked = false
          selectAll.indeterminate = false
        } else {
          selectAll.indeterminate = true
        }
      })
    }
  }

  def handleResize = Signal.dynamic {
    val element = Option(document.querySelector(s"#$id"))
    if (element.nonEmpty) {
      val maxWidth = element.get.getBoundingClientRect().width - remToPx(4.5)
      val items = options.value
        .filter(v => value.value.contains(v.id))
      val rect = element.get.querySelector(s".multiselect-value-wrapper").getBoundingClientRect()
      if (maxWidth > 0 && items.nonEmpty && rect.width > maxWidth) {
        val widths = items.map(v => v.displayWidth("pl-2 pr-7").value)
        var widthAcc = 0.0
        var visibleItemsCount = 0

        widths.foreach(w => {
          if (widthAcc + w <= maxWidth) {
            console.log(w)
            widthAcc += w
            visibleItemsCount += 1
          } else {
            visibleItems.set(visibleItemsCount)
          }
        })
      }
    }
  }

  def render: VMod = {
    val resizeObserver = ResizeObserver((entries, _) => {
      entries.foreach(entry => {
        handleResize
      })
    })

    value.observe(updateSelectAll)
    value.observe(_ => handleResize)

    div(
      onDomMount.foreach(element => {
        resizeObserver.observe(element.querySelector(".multiselect-value-wrapper"))
      }),
      onDomUnmount.foreach(element => {
        resizeObserver.disconnect()
        cleanPopper(s"#$id .multiselect-select")
      }),
      cls := "multiselect-dropdown dropdown bg-slate-50 relative w-full h-9 dark:bg-gray-700 border border-gray-300 dark:border-none overflow-hidden",
      cls <-- Signal { if (dropdownOpen.value) Some("dropdown-open") else None },
      props,
      idAttr := id,
      div(
        cls := "multiselect-select flex flex-row w-full h-full items-center",
        onClick.foreach(_ => {
          dropdownOpen.transform(wasOpen => {
            if (wasOpen) {
              cleanPopper(s"#$id .multiselect-select")
            } else {
              cleanPopper(s"#$id .multiselect-select")
              createPopper(s"#$id .multiselect-select", s"#$id .multiselect-dropdown-list-wrapper")
            }
            !wasOpen
          })
        }),
        Signal {
          input(
            outwatch.dsl.value := value.value.mkString(", "),
            tpe := "text",
            outwatch.dsl.required := required,
            cls := "peer/multiselect w-[1px] focus:outline-none opacity-0 border-none max-w-[1px] pointer-events-none absolute",
            tabIndex := -1,
            formId := props
              .collectFirst {
                case AccumAttr("form", value, _) => value
                case BasicAttr("form", value)    => value
              }
              .getOrElse("")
              .toString,
          )
        },
        div(
          cls := "flex flex-row w-full h-full items-center pl-2 text-slate-600",
          cls <-- Signal {
            if (styleValidity && dropdownOpen.value)
              "peer-invalid/multiselect:bg-red-100 peer-invalid/multiselect:text-red-600 peer-invalid/multiselect:border-red-600"
            else ""
          },
          div(
            cls := "flex flex-row gap-2 multiselect-value-wrapper",
            Signal.dynamic {
              options.value
                .filter(v => value.value.contains(v.id))
                .sortBy(_.displayWidth().value)
                .slice(0, visibleItems.value)
                .map(option => {
                  div(
                    cls := "bg-slate-300 text-slate-600 px-2 py-0.5 rounded-md flex flex-row gap-1 items-center whitespace-nowrap dark:bg-gray-500 dark:text-gray-200 multiselect-item",
                    option.name,
                    div(
                      icons.Close(cls := "w-4 h-4 text-slate-600 dark:text-slate-200"),
                      cls := "cursor-pointer",
                      onClick.foreach(_ => {
                        onInput(
                          getIds(onlyChecked = true)
                            .filter(id => id != option.id)
                            .asInstanceOf[Seq[String]],
                        )
                      }),
                    ),
                  )
                })
            },
            Signal {
              if (value.value.size > visibleItems.value) {
                Some(
                  div(
                    cls := "flex items-center justify-center text-slate-400 dark:text-gray-200",
                    if (visibleItems.value == 0) "Open to see more... " else "",
                    s"+${value.value.size - visibleItems.value}",
                  ),
                )
              } else if (value.value.isEmpty) {
                Some(
                  div(
                    if (!styleValidity)
                      cls := "text-slate-400 dark:text-gray-400"
                    else None,
                    cls := "flex items-center justify-center",
                    "Select...",
                  ),
                )
              } else None
            },
          ),
          label(
            tabIndex := 0,
            cls := "grow relative pr-7 h-full",
            div(cls := "absolute right-2 top-1/2 -translate-y-1/2", icons.Notch(cls := "w-4 h-4")),
          ),
        ),
      ),
      div(
        cls := "!fixed multiselect-dropdown-list-wrapper dark:bg-gray-700 dark:border-gray-700 bg-white dropdown-content !transition-none shadow-lg w-full rounded top-0 left-0 border border-gray-300 !z-[100]",
        if (searchEnabled) {
          Some(renderSearch)
        } else None,
        renderSelectAll,
        renderOptions,
      ),
    )
  }

  private def getIds(onlyChecked: Boolean): Seq[String] =
    document
      .querySelectorAll(s"#$id input[type=checkbox]:not(#all-checkbox-$id)" + (if (onlyChecked) ":checked" else ""))
      .toSeq
      .map(element =>
        element
          .asInstanceOf[HTMLElement]
          .dataset
          .get("id")
          .getOrElse(""),
      )

  private def renderSelectAll: VMod = div(
    cls := "p-2 border-b border-gray-300 dark:border-gray-600",
    label(
      Checkbox(
        CheckboxStyle.Default,
        cls := "mr-2",
        idAttr := s"all-checkbox-$id",
        onClick.foreach(e => {
          if (e.target.asInstanceOf[HTMLInputElement].checked) {
            onInput(getIds(onlyChecked = false))
          } else {
            onInput(Seq.empty)
          }
        }),
      ),
      forId := s"all-checkbox-$id",
      cls := "w-full block flex items-center",
      tabIndex := 0,
      "Select All",
    ),
  )

  private def renderSearch: VMod = input(
    cls := "multiselect-dropdown-search p-2 w-full focus:outline-0 border-b border-gray-300 dark:bg-gray-700 dark:border-gray-600",
    placeholder := "Search Options...",
    outwatch.dsl.onInput.value --> search,
    outwatch.dsl.value <-- search,
  )

  private def renderOptions: VMod = div(
    cls := "multiselect-dropdown-list max-h-96 md:max-h-44 sm:max-h-44 overflow-y-auto custom-scrollbar",
    Signal.dynamic {
      val options = this.options.value
        .filter(uiOption =>
          search.value.isBlank || uiOption.name.value.toLowerCase().nn.contains(search.value.toLowerCase()),
        )
        .map(renderOption)

      if (options.isEmpty) {
        Seq(renderNoOptions)
      } else {
        options
      }
    },
  )

  private def renderOption(uiOption: SelectOption): VMod = label(
    cls := "block w-full hover:bg-slate-50 px-2 py-0.5 flex items-center dark:hover:bg-gray-700",
    Checkbox(
      CheckboxStyle.Default,
      cls := "mr-2",
      checked <-- Signal { value.value.contains(uiOption.id) },
      idAttr := s"$id-${uiOption.id}",
      VMod.attr("data-id") := uiOption.id,
      onClick.foreach(_ => {
        onInput(getIds(onlyChecked = true))
      }),
    ),
    tabIndex := 0,
    uiOption.render,
    forId := s"$id-${uiOption.id}",
  )

  private def renderNoOptions: VMod = div(
    cls := "p-2 flex items-center justify-center text-slate-500 text-sm",
    emptyState,
  )
}

object MultiSelect {

  def apply(
      options: Signal[Seq[SelectOption]],
      onInput: (value: Seq[String]) => Unit,
      value: Signal[Seq[String]],
      showItems: Int = 5,
      searchEnabled: Boolean = true,
      emptyState: VMod = span("Nothing found..."),
      required: Boolean = false,
      props: VMod*,
  ): VMod = new MultiSelect(
    options,
    onInput,
    value,
    showItems,
    searchEnabled,
    emptyState,
    required = required,
    styleValidity = required,
    props,
  ).render

}
