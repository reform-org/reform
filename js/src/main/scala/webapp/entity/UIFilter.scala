package webapp.entity

import rescala.default.*
import webapp.given
import outwatch.*
import outwatch.dsl.*

trait UIFilter[EntityType] {
  def render: VNode

  val predicate: Signal[EntityType => Boolean]
}

class UISubstringFilter[EntityType, AttributeType](uiAttribute: UIAttribute[EntityType, AttributeType]) extends UIFilter[EntityType] {

  private val search = Var("")

  def render: VNode = {
    td(
      input(
        placeholder := "Filter here",
        value <-- search,
        onChange.value --> search,
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    search.map(s =>
      e => uiAttribute.getter(e).get.exists(
        v => uiAttribute.readConverter(v).toLowerCase.contains(s.toLowerCase)
      ),
    )
  }
}
