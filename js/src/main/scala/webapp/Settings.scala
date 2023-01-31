package webapp

import org.scalajs.dom.{window, console}
import scala.scalajs.js

object Settings {
    def get[T](name: String): Option[T] = {
        val settings = window.localStorage.getItem("settings")
        if(settings == null || settings.isBlank()) return None
        
        val settingsJSON = js.JSON.parse(settings)
        try{
            if(settingsJSON.selectDynamic(name) == js.undefined) return None
            return Some(settingsJSON.selectDynamic(name).asInstanceOf[T])
        }catch{
            case e => return None
        }
    }

    def set[T](name: String, value: T): Unit = {
        val settings = window.localStorage.getItem("settings")
        var newSettings = js.Object()
        if(settings != null && !settings.isBlank()){
            newSettings = js.JSON.parse(settings).asInstanceOf[js.Object]
        }

        newSettings.asInstanceOf[js.Dynamic].updateDynamic(name)(value.asInstanceOf[js.Any])

        window.localStorage.setItem("settings", js.JSON.stringify(newSettings))
    }
}