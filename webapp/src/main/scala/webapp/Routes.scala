package webapp

import colibri._
import colibri.router._
import colibri.router.Router
import org.scalajs.dom.window
import outwatch._
import rescala.default._
import webapp.pages._
import webapp.services._

object Routes:
  val fromPath: Path => Page =
    case Root                  => HomePage()
    case Root / "login"        => LoginPage()
    case Root / "project" / id => ProjectPage(id)

  val toPath: Page => Path =
    case HomePage()      => Root / ""
    case LoginPage()     => Root / "login"
    case ProjectPage(id) => Root / "project" / id
