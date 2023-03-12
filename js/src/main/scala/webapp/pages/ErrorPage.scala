package webapp.pages

import webapp.services.*
import webapp.webrtc.WebRTCService
import outwatch.*
import outwatch.dsl.*
import webapp.components.navigationHeader
import webapp.*
import org.scalajs.dom.HTMLElement
import webapp.npm.IIndexedDB
import webapp.JSImplicits

case class ErrorPage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render: VNode = {
    navigationHeader(
      div(
        cls := "flex items-center justify-center h-full w-full flex-col gap-6",
        h1("404 | Page not found", cls := "text-6xl text-slate-200"),
        a(
          "Take me Home",
          cls := "text-blue-600 text-lg",
          onClick.foreach(e => {
            e.preventDefault()
            e.target.asInstanceOf[HTMLElement].blur()
            jsImplicits.routing.to(HomePage())
          }),
          href := jsImplicits.routing.linkPath(HomePage()),
        ),
      ),
    )
  }
}
