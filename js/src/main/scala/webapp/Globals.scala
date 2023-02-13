package webapp

import scala.concurrent.ExecutionContext

// macrotask executor breaks indexeddb
given ExecutionContext = scala.concurrent.ExecutionContext.global

object Globals {
  val discoveryServerURL = "https://discovery.lukasschreiber.com"
  val discoveryServerWebsocketURL = "wss://wss.discovery.lukasschreiber.com"
  val turnServerURL = "turn:lukasschreiber.com:41720"
}
