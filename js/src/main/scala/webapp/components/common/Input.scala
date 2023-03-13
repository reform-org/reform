package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js

// TODO FIXME outwatch should at least give us a possibility so this value is stable.
def getID(name: String): String = {
  s"$name-${js.Math.round(js.Math.random() * 1000000)}"
}

def Label(props: VMod*): VNode = {
  label(cls := "label label-text text-slate-500 dark:text-slate-300", props)
}

def Input(props: VMod*): VNode = {
  input(
    cls := "input input-bordered w-full text-sm p-2 h-fit dark:bg-gray-700 dark:placeholder-gray-400 dark:text-slate-300",
    props,
  )
}

def TableInput(props: VMod*): VNode = {
  input(
    cls := "dark:border-gray-500 input valid:bg-green-100/50 valid:text-green-600 valid:placeholder-green-600 invalid:bg-yellow-100/50 invalid:placeholder-yellow-600 invalid:text-yellow-600 dark:valid:bg-green-100 dark:valid:text-green-600 dark:valid:placeholder-green-600 dark:invalid:bg-yellow-100 dark:invalid:placeholder-yellow-600 dark:invalid:text-yellow-600 bg-gray-50 input-ghost dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white !outline-0 rounded-none w-full border border-gray-300 h-9",
    props,
  )
}
def FileInput(props: VMod*): VNode = {
  input(
    cls := "file-input w-full text-sm h-fit",
    props,
  )
}

def LabeledInput(labelProps: VMod*)(props: VMod*): VMod = {
  val id = getID("input") // might not work if id attr is set
  div(Label(labelProps, cls := "pb-0.5", forId := id), Input(props, idAttr := id))
}
