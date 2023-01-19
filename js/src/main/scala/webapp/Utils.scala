package webapp

import outwatch.*
import outwatch.dsl.*

def duplicateValuesHandler[T <: outwatch.VModifier](values: List[T]) = {
  List(
    {
      Some(span(values.headOption.getOrElse("not initialized")))
    }, {
      val res = if (values.size > 1) {
        import outwatch.dsl.svg.*
        Some(
          span(
            cls := "tooltip tooltip-error",
            data.tip := "Conflicting values found. Edit to see all values and decide on a single value.",
            button(
              svg(
                xmlns := "http://www.w3.org/2000/svg",
                fill := "none",
                viewBox := "0 0 24 24",
                VModifier.attr("stroke-width") := "1.5",
                stroke := "currentColor",
                cls := "w-6 h-6",
                path(
                  VModifier.attr("stroke-linecap") := "round",
                  VModifier.attr("stroke-linejoin") := "round",
                  d := "M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z",
                ),
              ),
            ),
          ),
        )
      } else {
        None
      };
      res
    },
  )
}
