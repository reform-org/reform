package de.tu_darmstadt.informatik.st.reform.components.common

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import de.tu_darmstadt.informatik.st.reform.{*, given}

def NumberCard(title: String, number: VMod, description: VMod, props: VMod*): VNode = {
  div(
    cls := "bg-white shadow-lg rounded-lg p-4 flex flex-col gap-4 w-fit min-h-36 min-w-36 justify-evenly dark:bg-gray-700",
    div(
      title,
    ),
    div(
      cls := "self-center text-7xl font-bold",
      number,
    ),
    div(
      cls := "self-center text-xs italic text-slate-400 dark:text-gray-400",
      description,
    ),
    props,
  )
}

def MoneyCard(title: String, number: VMod, description: VMod, props: VMod*): VNode = {
  div(
    cls := "bg-white shadow-lg rounded-lg p-4 flex flex-col gap-4 w-fit min-h-36 min-w-36 justify-evenly dark:bg-gray-700",
    div(
      title,
    ),
    div(
      cls := "self-center flex align-start font-bold",
      div(cls := "text-5xl", number),
      div(cls := "text-xl", "€"),
    ),
    div(
      cls := "self-center text-xs italic text-slate-400 dark:text-gray-400",
      description,
    ),
    props,
  )
}

def TableCard(
    title: VMod,
    subtitle: VMod,
    header: Seq[VMod],
    rows: Seq[Seq[VMod]],
    footer: Seq[VMod],
    props: VMod*,
): VNode = {
  div(
    cls := "bg-white shadow-lg rounded-lg p-4 flex flex-col gap-4 min-w-fit overflow-x-auto custom-scrollbar max-w-[900px] w-full dark:bg-gray-700",
    div(
      cls := "",
      span(cls := "font-bold text-2xl", title),
      " ",
      span(cls := "italic text-slate-400 dark:text-gray-400", subtitle),
    ),
    table(
      thead(
        cls := "border-b border-slate-200",
        tr(header.map(th(cls := "text-left min-w-24 p-2", _))),
      ),
      tbody(
        rows.map(row => tr(row.map(td(cls := "min-w-24 p-2", _)))),
      ),
      tfoot(
        tr(footer.map(td(cls := "min-w-24 p-2", _))),
      ),
    ),
    props,
  )
}
