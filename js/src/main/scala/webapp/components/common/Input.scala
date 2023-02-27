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
    cls := "dark:border-gray-500 input valid:input-success bg-gray-50 input-ghost dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white !outline-0 rounded-none w-full border border-gray-300 h-9",
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
