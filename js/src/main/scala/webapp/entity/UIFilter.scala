package webapp.entity

import rescala.default.*
import webapp.given
import outwatch.*
import outwatch.dsl.*
import rescala.default
import webapp.components.common.*
import webapp.toQueryParameterName
import webapp.services.RoutingService
import webapp.JSImplicits

trait UIFilter[EntityType] {
  def render: VMod

  val predicate: Signal[EntityType => Boolean]
}

class UIFilterNothing[EntityType]() extends UIFilter[EntityType] {

  def render: VMod = None

  val predicate: Signal[EntityType => Boolean] = Signal(_ => true)
}

class UISubstringFilter[EntityType, AttributeType](uiAttribute: UIAttribute[EntityType, AttributeType])(using
    jsImplicits: JSImplicits,
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VMod = {
    div(
      cls := "max-w-[300px] min-w-[300px]",
      uiAttribute.label,
      Input(
        placeholder := "Filter here",
        value <-- jsImplicits.routing.getQueryParameterAsString(name),
        onInput.value.foreach(v => jsImplicits.routing.updateQueryParameters(Map(name -> v))),
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    jsImplicits.routing
      .getQueryParameterAsString(name)
      .map(s =>
        e => uiAttribute.getter(e).get.forall(v => uiAttribute.readConverter(v).toLowerCase.nn.contains(s.toLowerCase)),
      )
  }
}

class UIIntervalFilter[EntityType, AttributeType](uiAttribute: UITextAttribute[EntityType, AttributeType])(using
    jsImplicits: JSImplicits,
)(implicit
    ordering: Ordering[AttributeType],
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VMod = {
    div(
      cls := "max-w-[300px] min-w-[300px]",
      uiAttribute.label,
      div(
        cls := "flex flex-row gap-2 items-center",
        Input(
          placeholder := "Minimum value",
          `type` := uiAttribute.fieldType,
          value <-- jsImplicits.routing.getQueryParameterAsString(name + ":min"),
          onInput.value.foreach(v => jsImplicits.routing.updateQueryParameters(Map(name + ":min" -> v))),
        ),
        "-",
        Input(
          placeholder := "Maximum value",
          `type` := uiAttribute.fieldType,
          value <-- jsImplicits.routing.getQueryParameterAsString(name + ":max"),
          onInput.value.foreach(v => jsImplicits.routing.updateQueryParameters(Map(name + ":max" -> v))),
        ),
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    jsImplicits.routing
      .getQueryParameterAsString(name + ":min")
      .map(min =>
        jsImplicits.routing
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
    jsImplicits: JSImplicits,
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VNode = {
    div(
      cls := "max-w-[300px] min-w-[300px]",
      uiAttribute.label,
      MultiSelect(
        uiAttribute.optionsForFilter.map(option => option.map(selOpt => SelectOption(selOpt.id, selOpt.name))),
        value => jsImplicits.routing.updateQueryParameters(Map(name -> value)),
        jsImplicits.routing.getQueryParameterAsSeq(name),
        5,
        true,
        span("Nothing found..."),
        false,
        cls := "rounded-md",
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    jsImplicits.routing
      .getQueryParameterAsSeq(name)
      .map(s => e => s.isEmpty || uiAttribute.getter(e).get.exists(a => s.contains(a)))
  }
}

class UIMultiSelectFilter[EntityType](
    uiAttribute: UIMultiSelectAttribute[EntityType] | UICheckboxListAttribute[EntityType],
)(using
    jsImplicits: JSImplicits,
) extends UIFilter[EntityType] {

  private val name = toQueryParameterName(uiAttribute.label)

  def render: VMod = {
    div(
      cls := "max-w-[300px] min-w-[300px]",
      uiAttribute.label,
      div(
        cls := "flex flex-col gap-2",
        Select(
          Signal(
            Seq(
              SelectOption("or", Signal("Contains at least one")),
              SelectOption("and", Signal("Contains all")),
              SelectOption("exact", Signal("Exact match")),
            ),
          ),
          value => jsImplicits.routing.updateQueryParameters(Map(name + ":mode" -> value)),
          jsImplicits.routing.getQueryParameterAsString(name + ":mode"),
          false,
          span("Nothing found..."),
          false,
          false,
          cls := "rounded-md",
        ),
        MultiSelect(
          uiAttribute match {
            case x: UIMultiSelectAttribute[EntityType]  => x.optionsForFilter
            case x: UICheckboxListAttribute[EntityType] => x.optionsForFilter
          },
          value => jsImplicits.routing.updateQueryParameters(Map(name -> value)),
          jsImplicits.routing.getQueryParameterAsSeq(name),
          5,
          true,
          span("Nothing found..."),
          false,
          cls := "rounded-md",
        ),
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    jsImplicits.routing
      .getQueryParameterAsString(name + ":mode")
      .map(mode =>
        jsImplicits.routing
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
    jsImplicits: JSImplicits,
) extends UIFilter[EntityType] {

  // has not been tested and is currently not usable over URL becuase we do not have any Boolean field sadly
  private val name = toQueryParameterName(uiAttribute.label)

  private val selected = Var("")

  def render: VNode = {
    div(
      cls := "max-w-[300px] min-w-[300px]",
      uiAttribute.label,
      MultiSelect(
        Signal(Seq(SelectOption("true", Signal("Yes")), SelectOption("false", Signal("No")))),
        value => jsImplicits.routing.updateQueryParameters(Map(name -> value)),
        jsImplicits.routing.getQueryParameterAsSeq(name),
        5,
        true,
        span("Nothing found..."),
        false,
        cls := "rounded-md min-w-full",
      ),
    )
  }

  val predicate: Signal[EntityType => Boolean] = {
    jsImplicits.routing
      .getQueryParameterAsSeq(name)
      .map(s => e => uiAttribute.getter(e).get.forall(v => s.isEmpty || s.map(p => p.toBoolean).contains(v)))
  }
}
