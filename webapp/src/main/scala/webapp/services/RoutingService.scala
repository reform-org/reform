package webapp.services

import colibri._
import colibri.router._
import colibri.router.Router
import org.scalajs.dom._
import org.scalajs.dom
import outwatch._
import outwatch.dsl._
import rescala.default._
import scala.reflect.Selectable._
import scala.scalajs.js
import webapp._
import webapp.pages._

trait Page:
  def render(using services: Services): VNode

class RoutingState(
  // if canReturn is true then the page will show in mobile mode
  // an go back arrow in the top left corner
  val canReturn: Boolean,
) extends js.Object

class RoutingService():
  private val page = Var[Page](Routes.fromPath(Path(window.location.pathname)))

  def render(using services: Services): Signal[VNode] =
    page.map(_.render)

  def to(newPage: Page, preventReturn: Boolean = false) =
    window.history.pushState(RoutingState(!preventReturn), "", linkPath(newPage))
    page.set(newPage)

  def toReplace(newPage: Page, preventReturn: Boolean = false) =
    window.history.replaceState(RoutingState(!preventReturn), "", linkPath(newPage))
    page.set(newPage)

  def link(newPage: Page) =
    URL(linkPath(newPage), window.location.href).toString

  def linkPath(newPage: Page) =
    Routes.toPath(newPage).pathString

  def back =
    window.history.back()

  def state =
    window.history.state.asInstanceOf[RoutingState]

  // Ensure initial path is correctly set
  // Example: for path "/counter" and pattern "counter/{number=0}" the
  //          url should be "/counter/0" and not "/counter"
  window.history.replaceState(RoutingState(false), "", linkPath(page.now))

  // Change path when url changes by user action
  window.onpopstate = _ => page.set(Routes.fromPath(Path(window.location.pathname)))
