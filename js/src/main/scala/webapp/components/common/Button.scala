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

enum TableButtonStyle(val props: VMod) {
  case Default
      extends TableButtonStyle(
        cls := "bg-slate-200 hover:bg-slate-300 text-slate-600",
      )
  case Primary
      extends TableButtonStyle(
        cls := "bg-purple-200 hover:bg-purple-300 text-purple-600",
      )
//   case Delete
//       extends TableButtonStyle(
//         cls := "bg-red-200 hover:bg-red-300 text-red-600 tooltip tooltip-top",
//         Icons.close("fill-red-600 w-4 h-4"),
//         data.tip := "Delete",
//       )
}

def TableButton[T <: VMod](style: TableButtonStyle)(props: T*): VNode = {
  TableButton(props, style.props)
}

def TableButton[T <: VMod](props: T*): VNode = {
  val hasStyle = props
    .filter(p => p.isInstanceOf[AccumAttr] && p.asInstanceOf[AccumAttr].title == "class")
    .exists(p => p.asInstanceOf[AccumAttr].value.toString().contains("bg-"))
  button(
    cls := "rounded px-2 py-0 h-fit uppercase font-bold",
    props,
    if (!hasStyle) Some(TableButtonStyle.Primary.props) else None,
  )
}

def IconButton[T <: VMod](style: ButtonStyle)(props: T*): VNode = {
  IconButton(props, style.props)
}

def IconButton[T <: VMod](props: T*): VNode = {
  val hasStyle = props
    .filter(p => p.isInstanceOf[AccumAttr] && p.asInstanceOf[AccumAttr].title == "class")
    .exists(p => p.asInstanceOf[AccumAttr].value.toString().contains("bg-"))
  button(
    cls := "btn btn-active p-2 h-fit min-h-10 mt-2 border-0 w-full",
    props,
    if (!hasStyle) Some(ButtonStyle.Primary.props) else None,
  )
}
