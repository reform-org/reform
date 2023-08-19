package de.tu_darmstadt.informatik.st.reform.pages

import de.tu_darmstadt.informatik.st.reform.services.*
import de.tu_darmstadt.informatik.st.reform.webrtc.WebRTCService
import outwatch.*
import outwatch.dsl.*
import de.tu_darmstadt.informatik.st.reform.*
import org.scalajs.dom.HTMLElement
import de.tu_darmstadt.informatik.st.reform.npm.IIndexedDB
import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.components.common.*

case class ErrorPage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render = {
    error("404 | Page not found", "Take me Home", HomePage())
  }

  def error(text: String, label: String, page: Page) = {
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
