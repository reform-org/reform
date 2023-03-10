package webapp.components.common

import rescala.default.*
import outwatch.*
import outwatch.dsl.*
import webapp.{*, given}

class BasicOption(
    val id: String,
    val name: Signal[String],
    val props: VMod*,
) {
  def render: VNode = {
    span(props, name)
  }
}
