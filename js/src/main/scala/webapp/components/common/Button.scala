package webapp.components.common

import outwatch.*
import outwatch.dsl.*

import webapp.components.Icons

enum ButtonStyle(val props: VMod) {
  case Primary extends ButtonStyle(cls := "bg-purple-400 hover:bg-purple-400 text-purple-800")
  case Default extends ButtonStyle(cls := "bg-slate-400 hover:bg-slate-400 text-slate-800")
  case Success extends ButtonStyle(cls := "bg-green-400 hover:bg-green-400 text-green-800")
  case Warning extends ButtonStyle(cls := "bg-yellow-400 hover:bg-yellow-400 text-yellow-800")
  case Error extends ButtonStyle(cls := "bg-red-400 hover:bg-red-400 text-red-800")
}

enum LightButtonStyle(val props: VMod) {
  case Primary extends LightButtonStyle(cls := "bg-purple-200 hover:bg-purple-300 text-purple-600")
  case Default extends LightButtonStyle(cls := "bg-slate-200 hover:bg-slate-300 text-slate-600")
  case Success extends LightButtonStyle(cls := "bg-green-200 hover:bg-green-300 text-green-600")
  case Warning extends LightButtonStyle(cls := "bg-yellow-200 hover:bg-yellow-300 text-yellow-600")
  case Error extends LightButtonStyle(cls := "bg-red-200 hover:bg-red-300 text-red-600")
}

def Button[T <: VMod](style: ButtonStyle)(props: T*): VNode = {
  Button(props, style.props)
}

def Button[T <: VMod](props: T*): VNode = {
  val hasStyle = props
    .filter(p => p.isInstanceOf[AccumAttr] && p.asInstanceOf[AccumAttr].title == "class")
    .exists(p => p.asInstanceOf[AccumAttr].value.toString().contains("bg-"))
  button(
    cls := "btn btn-active p-2 h-fit min-h-10 mt-2 border-0 w-full",
    props,
    if (!hasStyle) Some(ButtonStyle.Primary.props) else None,
  )
}

def TableButton[T <: VMod](style: LightButtonStyle)(props: T*): VNode = {
  TableButton(props, style.props)
}

def TableButton[T <: VMod](props: T*): VNode = {
  val hasStyle = props
    .filter(p => p.isInstanceOf[AccumAttr] && p.asInstanceOf[AccumAttr].title == "class")
    .exists(p => p.asInstanceOf[AccumAttr].value.toString().contains("bg-"))
  button(
    cls := "rounded px-2 py-0 h-fit uppercase font-bold text-sm",
    props,
    if (!hasStyle) Some(LightButtonStyle.Primary.props) else None,
  )
}

def IconButton[T <: VMod](style: LightButtonStyle)(props: T*): VNode = {
  IconButton(props, style.props)
}

def IconButton[T <: VMod](props: T*): VNode = {
  val hasStyle = props
    .filter(p => p.isInstanceOf[AccumAttr] && p.asInstanceOf[AccumAttr].title == "class")
    .exists(p => p.asInstanceOf[AccumAttr].value.toString().contains("bg-"))
  button(
    cls := "p-0.5 h-fit w-fit cursor-pointer rounded-md",
    props,
    if (!hasStyle) Some(LightButtonStyle.Primary.props) else None,
  )
}