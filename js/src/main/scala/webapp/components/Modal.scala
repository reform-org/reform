package webapp.components

import outwatch.VNode
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.given
import org.scalajs.dom.{document, Element}
import webapp.components.common.*

class ModalButton(
    val text: String,
    val style: ButtonStyle = ButtonStyle.Default,
    val callback: () => Unit = () => {},
    val customAttributes: Seq[VMod] = Seq(),
)

class Modal(val title: VMod, val body: VMod, val buttons: Seq[ModalButton]) {
  private val openState = Var(false)

  def this(title: String = "", text: String = "", buttons: Seq[ModalButton] = Seq()) =
    this(title, span(text), buttons)

  document.addEventListener(
    "click",
    event => {
      if (event.target.asInstanceOf[Element].matches(".modal.modal-open")) {
        this.openState.set(false)
      }
    },
  )

  def button(): VNode = {
    Button(
      ButtonStyle.Default,
      "Open Modal",
      onClick.foreach(_ => this.openState.set(true)),
    )
  }

  def open(): Unit = {
    this.openState.set(true)
  }

  def close(): Unit = {
    this.openState.set(false)
  }

  def render: VMod = {
    div(
      div(
        cls <-- openState.map(v => s"modal ${(if (v) Some("modal-open") else None).getOrElse("")}"),
        div(
          cls := "modal-box",
          h3(cls := "font-bold text-xl", title),
          div(cls := "divider"),
          p(
            cls := "py-4",
            body,
          ),
          div(cls := "divider"),
          div(
            cls := "modal-action flex flex-row",
            buttons.map(button =>
              Button(
                button.style,
                button.text,
                onClick.foreach(_ => {
                  button.callback()
                  this.close()
                }),
                button.customAttributes,
              ),
            ),
          ),
        ),
      ),
    )
  }
}
