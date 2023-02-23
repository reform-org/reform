package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js

enum CheckboxStyle(val props: VMod*) {
  case Primary
      extends CheckboxStyle(
        cls := "border-purple-400 text-purple-800",
        styleAttr := "--chkbg:360 0% 100%; --chkfg: 271 81% 56%; background-color: white",
      )
  case Default
      extends CheckboxStyle(
        cls := "border-blue-400 text-blue-800",
        styleAttr := "--chkbg:360 0% 100%; --chkfg: 221 83% 53%; background-color: white",
      )
  case Success
      extends CheckboxStyle(
        cls := "border-green-400 text-green-800",
        styleAttr := "--chkbg:360 0% 100%; --chkfg: 142 76% 36%; background-color: white",
      )
  case Warning
      extends CheckboxStyle(
        cls := "border-yellow-400 text-yellow-800",
        styleAttr := "--chkbg:360 0% 100%; --chkfg: 41 96% 40%; background-color: white",
      )
  case Error
      extends CheckboxStyle(
        cls := "border-red-400 text-red-800",
        styleAttr := "--chkbg:360 0% 100%; --chkfg: 0 72% 51%; background-color: white",
      )
}

def Checkbox(style: CheckboxStyle, props: VMod*): VNode = {
  input(
    props,
    style.props,
    tpe := "checkbox",
    cls := "checkbox checkbox-xs rounded",
  )
}

def LabeledCheckbox(labelProps: VMod*)(style: CheckboxStyle, props: VMod*): VNode = {
  val id = s"${js.Math.round(js.Math.random() * 1000000)}"
  div(
    cls := "form-control",
    label(
      input(
        props,
        style.props,
        tpe := "checkbox",
        cls := "checkbox checkbox-xs rounded",
        idAttr := s"all-checkbox-$id",
      ),
      forId := s"all-checkbox-$id",
      cls := "label cursor-pointer",
      span(cls := "label-text", labelProps),
    ),
  )
}
