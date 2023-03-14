package webapp.components.common

import outwatch.*
import outwatch.dsl.*

enum ButtonStyle(val props: VMod) {
  case Primary
      extends ButtonStyle(
        cls := "bg-purple-400 disabled:bg-purple-400 disabled:text-purple-800 hover:bg-purple-400 text-purple-800",
      )
  case Default
      extends ButtonStyle(
        cls := "bg-slate-400 disabled:bg-slate-400 disabled:text-slate-800 hover:bg-slate-400 text-slate-800",
      )
  case Success
      extends ButtonStyle(
        cls := "bg-green-400 disabled:bg-green-400 disabled:text-green-800 hover:bg-green-400 text-green-800",
      )
  case Warning
      extends ButtonStyle(
        cls := "bg-yellow-400 disabled:bg-yellow-400 disabled:text-yellow-800 hover:bg-yellow-400 text-yellow-800",
      )
  case Error
      extends ButtonStyle(cls := "bg-red-400 disabled:bg-red-400 disabled:text-red-800 hover:bg-red-400 text-red-800")
  case LightPrimary
      extends ButtonStyle(
        cls := "bg-purple-200 disabled:bg-purple-200 disabled:text-purple-600 hover:bg-purple-300 text-purple-600",
      )
  case LightDefault
      extends ButtonStyle(
        cls := "bg-slate-200 disabled:bg-slate-200 disabled:text-slate-600 hover:bg-slate-300 text-slate-600 dark:bg-gray-700 dark:text-gray-200",
      )
  case LightSuccess
      extends ButtonStyle(
        cls := "bg-green-200 disabled:bg-green-200 disabled:text-green-600 hover:bg-green-300 text-green-600",
      )
  case LightWarning
      extends ButtonStyle(
        cls := "bg-yellow-200 disabled:bg-yellow-200 disabled:text-yellow-600 hover:bg-yellow-300 text-yellow-600",
      )
  case LightError
      extends ButtonStyle(cls := "bg-red-200 disabled:bg-red-200 disabled:text-red-600 hover:bg-red-300 text-red-600")
}

def Button(style: ButtonStyle, props: VMod*): VNode = {
  button(
    cls := "btn btn-active p-2 h-fit min-h-10 border-0 disabled:line-through disabled:opacity-[.5]",
    props,
    style.props,
  )
}

def TableButton(style: ButtonStyle, props: VMod*): VNode = {
  button(
    cls := "rounded px-2 py-1 h-fit uppercase font-bold text-sm",
    props,
    style.props,
  )
}

def IconButton(style: ButtonStyle, props: VMod*): VNode = {
  button(
    cls := "p-0.5 h-fit w-fit cursor-pointer rounded-md",
    props,
    style.props,
  )
}
