package webapp.components
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import webapp.given
import webapp.pages.*
import org.scalajs.dom.{console, document, window, HTMLElement}
import loci.transmitter.RemoteRef
import org.scalajs.dom.RTCStatsReport
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.webrtc.WebRTCService

def connectionRow(name: String, source: String, ref: RemoteRef)(using webrtc: WebRTCService) = {
  div(
    cls := "flex items-center justify-between p-2 hover:bg-slate-100 rounded-md",
    div(
      cls := "flex flex-col text-sm",
      div(
        name,
        cls := "font-bold",
      ),
      i(
        "Source: ",
        source,
        cls := "text-slate-500 text-xs",
      ),
      i(
        "Connection: ",
        Signals.fromFuture(webrtc.getConnectionMode(ref)),
        cls := "text-slate-500 text-xs",
      ),
    ),
    div(
      Icons.close("fill-red-600 w-4 h-4"),
      cls := "hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
      onClick.foreach(_ => ref.disconnect()),
    ),
  )
}
