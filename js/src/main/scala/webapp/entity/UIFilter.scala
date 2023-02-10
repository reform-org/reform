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

class UIIntervalFilter[EntityType, AttributeType](uiAttribute: UIAttribute[EntityType, AttributeType])(implicit
    ordering: Ordering[AttributeType],
) extends UIFilter[EntityType] {

  private val min = Var("")

  private val max = Var("")

  def render: VNode = {
    td(
      input(
        placeholder := "Minimum value",
        `type` := uiAttribute.fieldType,
        value <-- min,
        onChange.value --> min,
      ),
      input(
        placeholder := "Maximum value",
        `type` := uiAttribute.fieldType,
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

  private def isBetween(min: String, value: AttributeType, max: String): Boolean = {
    if (!min.isBlank) {
      val minVal = uiAttribute.writeConverter(min)
      if (ordering.gt(minVal, value)) {
        return false
      }
    }

    if (!max.isBlank) {
      val maxVal = uiAttribute.writeConverter(max)
      if (ordering.lt(maxVal, value)) {
        return false
      }
    }

    true
  }
}
