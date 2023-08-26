package de.tu_darmstadt.informatik.st.reform.pages

import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.components.common.*
import de.tu_darmstadt.informatik.st.reform.services.*
import org.scalajs.dom.HTMLElement
import outwatch.*
import outwatch.dsl.*

case class ErrorPage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render: VMod = {
    error("404 | Page not found", "Take me Home", HomePage())
  }

  def error(text: String, label: String, page: Page): VNode = {
    div(
      cls := "flex items-center justify-center h-[80vh] w-screen flex-col gap-6",
      h1(text, cls := "text-6xl text-gray-200"),
      Button(
        ButtonStyle.Primary,
        label,
        onClick.foreach(e => {
          e.preventDefault()
          e.target.asInstanceOf[HTMLElement].blur()
          jsImplicits.routing.to(page)
        }),
        href := jsImplicits.routing.linkPath(page),
      ),
    )
  }
}
