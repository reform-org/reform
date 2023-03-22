package webapp.pages

import webapp.services.*
import webapp.webrtc.WebRTCService
import outwatch.*
import outwatch.dsl.*
import webapp.*
import org.scalajs.dom.HTMLElement
import webapp.npm.IIndexedDB
import webapp.JSImplicits
import webapp.components.common.*
import rescala.default.*
import webapp.given

case class ErrorPage(code: Int = 404, title: String = "", description: String = "")(using
    jsImplicits: JSImplicits,
) extends Page {

  def render = Signal {
    val querytitle = jsImplicits.routing.getQueryParameterAsString("title").value
    val querycode = jsImplicits.routing.getQueryParameterAsString("code").value
    val querydescription = jsImplicits.routing.getQueryParameterAsString("description").value

    println(querydescription)

    error(
      s"${if (querycode.isBlank()) this.code.toString() else querycode} | ${if (querytitle.isBlank()) this.title
        else querytitle}",
      if (querydescription.isBlank()) this.description else querydescription,
      "Take me Home",
      HomePage(),
    )
  }

  def error(text: String, description: String, label: String, page: Page) = {
    div(
      cls := "flex items-center justify-center h-[80vh] w-full flex-col gap-6",
      h1(text, cls := "text-6xl text-gray-200"),
      div(description),
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
