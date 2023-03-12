package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js

enum CheckboxStyle(val props: VMod*) {
  case Primary
      extends CheckboxStyle(
        cls := "border-purple-400 text-purple-800 ",
        styleAttr := "--chkfg: 271 81% 56%",
      )
  case Default
      extends CheckboxStyle(
        cls := "border-blue-400 text-blue-800 ",
        styleAttr := "--chkfg: 221 83% 53%",
      )
  case Success
      extends CheckboxStyle(
        cls := "border-green-400 text-green-800 ",
        styleAttr := "--chkfg: 142 76% 36%",
      )
  case Warning
      extends CheckboxStyle(
        cls := "border-yellow-400 text-yellow-800 ",
        styleAttr := "--chkfg: 41 96% 40%",
      )
  case Error
      extends CheckboxStyle(
        cls := "border-red-400 text-red-800 ",
        styleAttr := "--chkfg: 0 72% 51%",
      )
}

def Checkbox(style: CheckboxStyle, props: VMod*): VNode = {
  input(
    props,
    style.props,
    styleAttr := "--chkbg:360 0% 100%",
    tpe := "checkbox",
    cls := "checkbox checkbox-xs rounded bg-white dark:bg-gray-700 dark:border-none !animate-none",
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
      cls := "label cursor-pointer !justify-start gap-2",
      span(cls := "label-text", labelProps),
    ),
  )
}
