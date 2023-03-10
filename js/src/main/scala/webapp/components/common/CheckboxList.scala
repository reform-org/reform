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
import webapp.utils.Seqnal.*

class CheckboxList(
    options: Signal[Seq[SelectOption]],
    onInput: (value: Seq[String]) => Unit,
    value: Signal[Seq[String]],
    emptyState: VMod = span("Nothing found..."),
    required: Boolean = false,
    props: VMod*,
) {

  private val id = s"checkbox-list-${js.Math.round(js.Math.random() * 100000)}"

  def render: VMod = {
    div(
      cls := "flex flex-row",
      props,
      idAttr := id,
      Signal {
        input(
          outwatch.dsl.value := value.value.mkString(", "),
          tpe := "text",
          outwatch.dsl.required := required,
          cls := "w-[1px] focus:outline-none opacity-0 border-none max-w-[1px] pointer-events-none",
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
      renderOptions,
    )
  }

  private def renderOptions: VMod = div(
    Signal.dynamic {
      val options = this.options.value.map(renderOption)

      if (options.isEmpty) {
        Seq(renderNoOptions)
      } else {
        options
      }
    },
  )

  private def renderOption(uiOption: SelectOption): VMod = label(
    cls := "block w-fit hover:bg-slate-50 px-2 py-0.5 flex items-center dark:hover:bg-gray-700 rounded-md cursor-pointer",
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
  )

  private def renderNoOptions: VMod = div(
    cls := "p-2 flex items-center justify-center text-slate-500 text-sm",
    emptyState,
  )
}

object CheckboxList {

  def apply(
      options: Signal[Seq[SelectOption]],
      onInput: (value: Seq[String]) => Unit,
      value: Signal[Seq[String]],
      emptyState: VMod = span("Nothing found..."),
      required: Boolean = false,
      props: VMod*,
  ): VMod = new CheckboxList(
    options,
    onInput,
    value,
    emptyState,
    required,
    props,
  ).render

}