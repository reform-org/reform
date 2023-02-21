package webapp

import outwatch.*
import outwatch.dsl.*

def duplicateValuesHandler[T <: outwatch.VMod](values: Seq[T]) = {
  div(
    cls := s"flex w-full flex-row justify-between items-center min-h-9 h-9 px-4 ${if (values.size > 1) "bg-yellow-200 py-0"
      else "py-1"}", {
      Some(span(values.headOption.getOrElse("not initialized")))
    }, {
      val res = if (values.size > 1) {
        import outwatch.dsl.svg.*
        Some(
          div(
            cls := "tooltip tooltip-error flex justify-center items-center",
            data.tip := "Conflicting values found. Edit to see all values and decide on a single value.",
            button(
              svg(
                xmlns := "http://www.w3.org/2000/svg",
                fill := "none",
                viewBox := "0 0 24 24",
                VMod.attr("stroke-width") := "1.5",
                stroke := "#eab308",
                cls := "w-6 h-6",
                path(
                  VMod.attr("stroke-linecap") := "round",
                  VMod.attr("stroke-linejoin") := "round",
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
