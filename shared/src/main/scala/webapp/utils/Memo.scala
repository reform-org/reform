package webapp.utils
import scala.scalajs.js
import js.Dynamic.global
import scalajs.js.DynamicImplicits.truthValue

def memo[A, R](f: A => R): A => R = {
  var map = global.WeakMap()

  (a: A) => {
    val value = map.get(a.asInstanceOf[js.Any])
    if (value) {
      value.asInstanceOf[R]
    } else {
      val value = f(a).asInstanceOf[R]
      map.put(a.asInstanceOf[js.Any], value.asInstanceOf[js.Any])
      value
    }
  }
}
