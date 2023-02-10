package webapp.entity

import rescala.default.*
import webapp.given
import outwatch.*
import outwatch.dsl.*

trait UIFilter[EntityType] {
  def render: VNode

  val predicate: Signal[EntityType => Boolean]
}

class UISubstringFilter[EntityType, AttributeType](uiAttribute: UIAttribute[EntityType, AttributeType])
    extends UIFilter[EntityType] {

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
      e => uiAttribute.getter(e).get.exists(v => uiAttribute.readConverter(v).toLowerCase.contains(s.toLowerCase)),
    )
  }
}

class UIIntervalFilter[EntityType](uiAttribute: UIAttribute[EntityType, Int]) extends UIFilter[EntityType] {

  private val min = Var("")

  private val max = Var("")

  def render: VNode = {
    td(
      input(
        placeholder := "Minimum value",
        value <-- min,
        onChange.value --> min,
      ),
      input(
        placeholder := "Maximum value",
        value <-- max,
        onChange.value --> max,
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    min
      .map(min =>
        max.map(max =>
          (e: EntityType) =>
            uiAttribute
              .getter(e)
              .get
              .exists(
                isBetween(min, _, max),
              ),
        ),
      )
      .flatten
  }

  private def isBetween(min: String, value: Int, max: String): Boolean = {
    val minInt = min.toIntOption.getOrElse(Integer.MIN_VALUE)
    val maxInt = max.toIntOption.getOrElse(Integer.MAX_VALUE)
    minInt <= value && value <= maxInt
  }
}
