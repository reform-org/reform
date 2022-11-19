package webapp.pages

import org.scalajs.dom
import outwatch._
import outwatch.dsl._
import rescala.default._
import webapp.services._
import webapp._

case class HomePage() extends Page:
  def render(using services: Services): VNode =
    div(
      h1(cls := "font-bold underline", "Hello world!"),
      cls := "h-56 grid grid-cols-3 gap-4 content-center",
      a(
        href := "/login",
        "Login",
        onClick.foreach(e => {
          e.preventDefault()
          services.routing.to(LoginPage(), true)
        }),
      ),
      a(
        href := "/project/Wir sind schon die besten lol",
        "Beispielprojekt",
        onClick.foreach(e => {
          e.preventDefault()
          services.routing.to(ProjectPage("Wir sind schon die besten lol"), true)
        }),
      ),
    )
