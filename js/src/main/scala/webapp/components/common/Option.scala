package webapp.components.common

import rescala.default.*
import outwatch.*
import outwatch.dsl.*
import webapp.{*, given}
import org.scalajs.dom.document
import cats.effect.SyncIO

class SelectOption(
    val id: String,
    val name: Signal[String],
    val props: VMod*,
) {
  def render: VNode = {
    span(props, name, cls := "overflow-hidden max-w-full text-ellipsis inline-block")
  }

  def displayWidth(classes: String = ""): Double = {
    val element = document.createElement("span")
    Outwatch
      .renderInto[SyncIO](
        element,
        span(props, name.now, cls := classes, styleAttr := "max-height: 0px !important; opacity: 0 !important"),
      )
      .unsafeRunSync()
    document.body.appendChild(element)
    val width = element.getBoundingClientRect().width
    document.body.removeChild(element)

    width
  }
}
