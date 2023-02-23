package webapp.components.common

import outwatch.*
import outwatch.dsl.*

enum ButtonStyle(val props: VMod) {
  case Primary extends ButtonStyle(cls := "bg-purple-400 hover:bg-purple-400 text-purple-800")
  case Default extends ButtonStyle(cls := "bg-slate-400 hover:bg-slate-400 text-slate-800")
  case Success extends ButtonStyle(cls := "bg-green-400 hover:bg-green-400 text-green-800")
  case Warning extends ButtonStyle(cls := "bg-yellow-400 hover:bg-yellow-400 text-yellow-800")
  case Error extends ButtonStyle(cls := "bg-red-400 hover:bg-red-400 text-red-800")
  case LightPrimary extends ButtonStyle(cls := "bg-purple-200 hover:bg-purple-300 text-purple-600")
  case LightDefault extends ButtonStyle(cls := "bg-slate-200 hover:bg-slate-300 text-slate-600")
  case LightSuccess extends ButtonStyle(cls := "bg-green-200 hover:bg-green-300 text-green-600")
  case LightWarning extends ButtonStyle(cls := "bg-yellow-200 hover:bg-yellow-300 text-yellow-600")
  case LightError extends ButtonStyle(cls := "bg-red-200 hover:bg-red-300 text-red-600")
}

def Button(style: ButtonStyle, props: VMod*): VNode = {
  button(
    cls := "btn btn-active p-2 h-fit min-h-10 mt-2 border-0",
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
