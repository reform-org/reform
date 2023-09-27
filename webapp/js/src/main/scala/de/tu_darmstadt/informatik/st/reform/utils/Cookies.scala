package webapp.utils

import org.scalajs.dom.document
import scala.scalajs.js

object Cookies {
  def getCookie(name: String): Option[String] = {
    document.cookie
      .split(";")
      .nn
      .find(cookie => {
        cookie.nn.split("=").nn(0) == name
      })
      .map(cookie => {
        val kv = cookie.nn.split("=").nn
        kv(1).nn
      })
  }

  def clearCookie(name: String): Unit = {
    document.cookie = s"$name=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
  }

  def setCookie(name: String, value: String, days: Long = 14) = {
    val date = new js.Date()
    date.setTime(date.getTime() + (days * 86400000))
    val expires = s"expires=${date.toUTCString()}"

    document.cookie = s"$name=$value; $expires; path=/"
  }
}
