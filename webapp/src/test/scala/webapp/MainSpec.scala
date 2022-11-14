package webapp

import org.scalajs.dom._
import outwatch._
import outwatch.dsl._

import cats.effect.SyncIO

class WebappSpec extends JSDomSpec {

  "You" should "probably add some tests" in {

    val message = "Hello World!"
    Outwatch.renderInto[SyncIO]("#app", h1(message)).unsafeRunSync()

    document.body.innerHTML.contains(message) shouldBe true
  }
}
