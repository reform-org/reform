package webapp.entity

import rescala.default.*
import webapp.given
import outwatch.*
import outwatch.dsl.*
import rescala.default
import webapp.components.common.*
import webapp.toQueryParameterName
import webapp.services.RoutingService

trait UIFilter[EntityType] {
  def render: VNode

  val predicate: Signal[EntityType => Boolean]
}

class UIFilterNothing[EntityType]() extends UIFilter[EntityType] {

  def render: VNode = td()

  val predicate: Signal[EntityType => Boolean] = Signal(_ => true)
}

class UISubstringFilter[EntityType, AttributeType](uiAttribute: UIAttribute[EntityType, AttributeType])(using
    routing: RoutingService,
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      uiAttribute.label,
      Input(
        placeholder := "Filter here",
        value <-- routing.getQueryParameterAsString(name),
        onInput.value.foreach(v => routing.updateQueryParameters(Map(name -> v))),
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    routing
      .getQueryParameterAsString(name)
      .map(s =>
        e => uiAttribute.getter(e).get.forall(v => uiAttribute.readConverter(v).toLowerCase.nn.contains(s.toLowerCase)),
      )
  }
}

class UIIntervalFilter[EntityType, AttributeType](uiAttribute: UITextAttribute[EntityType, AttributeType])(using
    routing: RoutingService,
)(implicit
    ordering: Ordering[AttributeType],
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      uiAttribute.label,
      Input(
        placeholder := "Minimum value",
        `type` := uiAttribute.fieldType,
        value <-- routing.getQueryParameterAsString(name + ":min"),
        onInput.value.foreach(v => routing.updateQueryParameters(Map(name + ":min" -> v))),
      ),
      Input(
        placeholder := "Maximum value",
        `type` := uiAttribute.fieldType,
        value <-- routing.getQueryParameterAsString(name + ":max"),
        onInput.value.foreach(v => routing.updateQueryParameters(Map(name + ":max" -> v))),
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    routing
      .getQueryParameterAsString(name + ":min")
      .map(min =>
        routing
          .getQueryParameterAsString(name + ":max")
          .map(max =>
            (e: EntityType) =>
              uiAttribute
                .getter(e)
                .get
                .forall(
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

class UISelectFilter[EntityType, AttributeType](uiAttribute: UISelectAttribute[EntityType, AttributeType])(using
    routing: RoutingService,
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      uiAttribute.label,
      MultiSelect(
        uiAttribute.optionsForFilter.map(option => option.map(selOpt => MultiSelectOption(selOpt.id, selOpt.name))),
        value => routing.updateQueryParameters(Map(name -> value)),
        routing.getQueryParameterAsSeq(name),
        5,
        true,
        span("Nothing found..."),
        false,
        cls := "rounded-md",
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    routing
      .getQueryParameterAsSeq(name)
      .map(s => e => s.isEmpty || uiAttribute.getter(e).get.exists(a => s.contains(a)))
  }
}

class UIMultiSelectFilter[EntityType](
    uiAttribute: UIMultiSelectAttribute[EntityType] | UICheckboxListAttribute[EntityType],
)(using
    routing: RoutingService,
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      uiAttribute.label,
      Select(
        Signal(
          Seq(
            SelectOption("or", Signal("Contains at least one")),
            SelectOption("and", Signal("Contains all")),
            SelectOption("exact", Signal("Exact match")),
          ),
        ),
        value => routing.updateQueryParameters(Map(name + ":mode" -> value)),
        routing.getQueryParameterAsString(name + ":mode"),
        false,
        span("Nothing found..."),
        false,
        cls := "rounded-md",
      ),
      MultiSelect(
        uiAttribute match {
          case x: UIMultiSelectAttribute[EntityType] => x.optionsForFilter
          case x: UICheckboxListAttribute[EntityType] =>
            x.optionsForFilter.map(_.map(option => MultiSelectOption(option.id, option.name, option.props)))
        },
        value => routing.updateQueryParameters(Map(name -> value)),
        routing.getQueryParameterAsSeq(name),
        5,
        true,
        span("Nothing found..."),
        false,
        cls := "rounded-md",
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    routing
      .getQueryParameterAsString(name + ":mode")
      .map(mode =>
        routing
          .getQueryParameterAsSeq(name)
          .map(s =>
            (e: EntityType) =>
              s.isEmpty || uiAttribute
                .getter(e)
                .get
                .exists(a => {
                  var res = false
                  if (mode == "or") {
                    res = s.toSet.intersect(a.toSet).nonEmpty
                  } else if (mode == "and") {
                    res = s.toSet.intersect(a.toSet).size >= s.toSet.size
                  } else if (mode == "exact") {
                    res = s.toSet.intersect(a.toSet).size == s.toSet.size && a.toSet.size == s.toSet.size
                  }
                  res
                }),
          ),
      )
      .flatten
  }
}

class UIBooleanFilter[EntityType](uiAttribute: UITextAttribute[EntityType, Boolean])(using
    routing: RoutingService,
) extends UIFilter[EntityType] {

  // has not been tested and is currently not usable over URL becuase we do not have any Boolean field sadly
  private val name = toQueryParameterName(uiAttribute.label)

  private val selected = Var("")

  def render: VNode = {
    div(
      uiAttribute.label,
      MultiSelect(
        Signal(Seq(MultiSelectOption("true", Signal("Yes")), MultiSelectOption("false", Signal("No")))),
        value => routing.updateQueryParameters(Map(name -> value)),
        routing.getQueryParameterAsSeq(name),
        5,
        true,
        span("Nothing found..."),
        false,
        cls := "rounded-md",
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    routing
      .getQueryParameterAsSeq(name)
      .map(s => e => uiAttribute.getter(e).get.forall(v => s.isEmpty || s.map(p => p.toBoolean).contains(v)))
  }
}
