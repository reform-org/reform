package webapp.pages

import org.scalajs.dom
import outwatch._
import outwatch.dsl._
import rescala.default._
import webapp.services._
import webapp._

case class LoginPage() extends Page:
  def render(using services: Services): VNode =
    div(
      h1(cls := "font-bold underline", "Login page"),
      cls := "h-56 grid grid-cols-3 gap-4 content-center",
      a(
        href := "/",
        "Home",
        onClick.foreach(e => {
          e.preventDefault()
          services.routing.to(HomePage(), true)
        }),
      ),
    )
