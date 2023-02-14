package webapp

import org.scalajs.dom.window

import scala.scalajs.js

object Settings {
  def get[T](name: String): Option[T] = {
    val settings = Option(window.localStorage.getItem("settings"))
    if (settings.isEmpty || settings.get.isBlank()) return None

    val settingsJSON = js.JSON.parse(settings.get)
    try {
      if (settingsJSON.selectDynamic(name) == js.undefined) return None
      return Some(settingsJSON.selectDynamic(name).asInstanceOf[T])
    } catch {
      case e => return None
    }
  }

  def set[T](name: String, value: T): Unit = {
    val settings = Option(window.localStorage.getItem("settings"))
    var newSettings = js.Object()
    if (settings.nonEmpty && !settings.get.isBlank()) {
      newSettings = js.JSON.parse(settings.get).asInstanceOf[js.Object]
    }

    newSettings.asInstanceOf[js.Dynamic].updateDynamic(name)(value.asInstanceOf[js.Any])

    window.localStorage.setItem("settings", js.JSON.stringify(newSettings))
  }
}
