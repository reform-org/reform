package webapp.components.common

import outwatch.*
import outwatch.dsl.*
import scala.scalajs.js

def getID(name: String): String = {
  s"$name-${js.Math.round(js.Math.random() * 1000000)}"
}

def Label[T <: VMod](props: T*): VNode = {
  label(cls := "label label-text text-slate-500", props)
}

def Input[T <: VMod](props: T*): VNode = {
  input(
    cls := "input input-bordered w-full text-sm p-2 h-fit",
    props,
  )
}

def TableInput[T <: VMod](props: T*): VNode = {
  input(
    cls := "input valid:input-success bg-gray-50 input-ghost dark:bg-gray-700 dark:placeholder-gray-400 dark:text-white !outline-0 rounded-none w-full border border-gray-300 h-9",
    props,
  )
}
def FileInput[T <: VMod](props: T*): VNode = {
  input(
    cls := "file-input w-full text-sm h-fit",
    props,
  )
}

def LabeledInput[T <: VMod](label: T*)(props: T*): VMod = {
  val id = getID("input") // might not work if id attr is set
  div(Label(label, cls := "pb-0.5", forId := id), Input(props, idAttr := id))
}
