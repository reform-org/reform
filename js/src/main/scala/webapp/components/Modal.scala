package webapp.components

import outwatch.VNode
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import org.scalajs.dom.{document, console}
import webapp.given
import org.scalajs.dom.Element

class ModalButton(val text: String, val classes: String = "", val callback: Option[() => Unit] = None)

class Modal(val title: String = "", val body: String = "", val buttons: Seq[ModalButton]) {
  private val openState = Var(false)

  document.addEventListener("click", event => {
    if(event.target.asInstanceOf[Element].matches(".modal.modal-open")){
        this.openState.set(false)
    }
  })

  def button(): VNode = {
    input(
      tpe := "button",
      cls := "btn btn-active p-2 h-fit min-h-10 border-0",
      value := "test",
      onClick.foreach(_ => this.openState.set(true)),
    )
  }

  def open() = {
    this.openState.set(true)
  }

  def close() = {
    this.openState.set(false)
  }

  def render(): VNode = {
    div(
      div(
        cls <-- openState.map(v => s"modal ${(if (v == true) Some("modal-open") else None).getOrElse("")}"),
        div(
          cls := "modal-box",
          h3(cls := "font-bold text-lg text-purple-600", title),
          p(
            cls := "py-4",
            body,
          ),
          div(
            cls := "modal-action flex flex-row",
            buttons.map(button =>
              label(
                cls := s"btn btn-active p-2 h-fit min-h-10 border-0 ${button.classes}",
                button.text,
                onClick.foreach(_ => {
                  button.callback match {
                    case None => this.close()
                    case Some(actualCallback) => {
                      actualCallback()
                      this.close()
                    }
                  }
                }),
              ),
            ),
          ),
        ),
      ),
    )
  }
}
