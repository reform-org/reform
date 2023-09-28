package de.tu_darmstadt.informatik.st.reform.components.common

import de.tu_darmstadt.informatik.st.reform.{*, given}
import org.scalajs.dom.document
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

class SelectOption(
    val id: String,
    val name: Signal[String],
    val props: VMod*,
) {
  def render: VMod = {
    span(props, name, cls := "overflow-hidden max-w-full text-ellipsis inline-block")
  }

  def displayWidth(classes: String = ""): Signal[Double] = Signal {
    val element = document.createElement("span")
    element.innerHTML =
      s"<span class='$classes' style='max-height: 0px !important; opacity: 0 !important'>${name.value}</span>"
    document.body.appendChild(element)
    val width = element.getBoundingClientRect().width
    document.body.removeChild(element)

    width
  }
}
