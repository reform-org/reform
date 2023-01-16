package webapp.utils
import scala.scalajs.js
import js.Dynamic.global
import scalajs.js.DynamicImplicits.truthValue
import scala.scalajs.js.annotation.JSName
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal
class WeakMap() extends js.Object {

  def get(key: js.Any): js.UndefOr[js.Any] = js.native

  def set(key: js.Any, value: js.Any): Unit = js.native
}

def memo[A, R](f: A => R): A => R = {
  var map = new WeakMap() // js.Dynamic.newInstance(js.Dynamic.global.WeakMap)()

  (a: A) => {
    val value = map.get(a.asInstanceOf[js.Any])
    if (value.nonEmpty) {
      value.asInstanceOf[R]
    } else {
      val value = f(a).asInstanceOf[R]
      map.set(a.asInstanceOf[js.Any], value.asInstanceOf[js.Any])
      value
    }
  }
}
