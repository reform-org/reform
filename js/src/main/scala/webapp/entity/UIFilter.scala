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

  val predicate: default.Signal[EntityType => Boolean] = Signal(_ => true)
}

class UISubstringFilter[EntityType, AttributeType](uiAttribute: UIAttribute[EntityType, AttributeType])(using
    routing: RoutingService,
) extends UIFilter[EntityType] {

  private val search = Var("")
  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      uiAttribute.label,
      Input(
        placeholder := "Filter here",
        value <-- routing.getQueryParameterAsString(name),
        onInput.value.foreach(v => routing.updateQueryParameters(Map((name -> v)))),
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    search.map(s =>
      e => uiAttribute.getter(e).get.exists(v => uiAttribute.readConverter(v).toLowerCase.contains(s.toLowerCase)),
    )
  }
}

class UIIntervalFilter[EntityType, AttributeType](uiAttribute: UITextAttribute[EntityType, AttributeType])(using
    routing: RoutingService,
)(implicit
    ordering: Ordering[AttributeType],
) extends UIFilter[EntityType] {

  private val min = Var("")

  private val max = Var("")
  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      uiAttribute.label,
      Input(
        placeholder := "Minimum value",
        `type` := uiAttribute.fieldType,
        value <-- min,
        onInput.value --> min,
      ),
      Input(
        placeholder := "Maximum value",
        `type` := uiAttribute.fieldType,
        value <-- max,
        onInput.value --> max,
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

class UISelectFilter[EntityType, AttributeType](uiAttribute: UISelectAttribute[EntityType, AttributeType])(using
    routing: RoutingService,
) extends UIFilter[EntityType] {

  private val selectValue: Var[Seq[String]] = Var(Seq())
  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      uiAttribute.label,
      MultiSelect(
        uiAttribute.options.map(option => option.map(selOpt => MultiSelectOption(selOpt.id, selOpt.name))),
        (value) => selectValue.set(value),
        selectValue,
        5,
        true,
        span("Nothing found..."),
        cls := "rounded-md",
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    selectValue.map(s => e => s.size == 0 || uiAttribute.getter(e).get.exists(a => s.contains(a)))
  }
}

class UIMultiSelectFilter[EntityType](uiAttribute: UIMultiSelectAttribute[EntityType])(using
    routing: RoutingService,
) extends UIFilter[EntityType] {

  private val selectValue: Var[Seq[String]] = Var(Seq())
  private val mode: Var[String] = Var("")
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
        (value) => mode.set(value),
        mode,
        false,
        span("Nothing found..."),
        cls := "rounded-md",
      ),
      MultiSelect(
        uiAttribute.options,
        (value) => selectValue.set(value),
        selectValue,
        5,
        true,
        span("Nothing found..."),
        cls := "rounded-md",
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    mode
      .map(mode =>
        selectValue
          .map(s =>
            (e: EntityType) =>
              s.size == 0 || uiAttribute
                .getter(e)
                .get
                .exists(a => {
                  var res = false;
                  if (mode == "or") {
                    res = s.toSet.intersect(a.toSet).size > 0
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

  private val selected = Var("")
  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    select(
      cls := "input valid:input-success",
      onInput.value --> selected,
      option(value := "both", "Both"),
      option(value := "true", "Yes"),
      option(value := "false", "No"),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    selected.map(s => e => uiAttribute.getter(e).get.exists(v => s.isBlank || s == "both" || s.toBoolean == v))
  }
}
