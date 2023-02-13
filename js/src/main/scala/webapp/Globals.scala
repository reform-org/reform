package webapp

import org.scalajs.macrotaskexecutor.MacrotaskExecutor
import scala.concurrent.ExecutionContext

given ExecutionContext = MacrotaskExecutor

object Globals {
  val discoveryServerURL = "https://discovery.lukasschreiber.com"
  val discoveryServerWebsocketURL = "wss://wss.discovery.lukasschreiber.com"
  val turnServerURL = "turn:lukasschreiber.com:41720"
}
