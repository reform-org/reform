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
import webapp.services.DiscoveryService
import webapp.services.AvailableConnection

def connectionRow(name: String, source: String, uuid: String, ref: RemoteRef)(using
    webrtc: WebRTCService,
    discovery: DiscoveryService,
) = {
  if (source == "discovery") {
    val own = discovery.decodeToken(discovery.getToken())
    div(
      cls := "flex items-center justify-between p-2 hover:bg-slate-100 rounded-md",
      div(
        cls := "flex flex-col text-sm",
        div(
          name,
          cls := "font-bold",
        ),
        i(
          span(
            "ID: ",
            cls := "text-slate-400",
          ),
          uuid.split("-")(0),
          cls := "text-slate-500 text-xs",
        ),
        i(
          span(
            "Source: ",
            cls := "text-slate-400",
          ),
          source,
          cls := "text-slate-500 text-xs",
        ),
        i(
          span(
            "Connection: ",
            cls := "text-slate-400",
          ),
          Signals.fromFuture(webrtc.getConnectionMode(ref)),
          cls := "text-slate-500 text-xs",
        ),
      ),
      div(
        cls := "flex flex-row gap-1",
        if (own.uuid != uuid)
          Some(
            div(
              Icons.forbidden("fill-red-600 w-3 h-3"),
              cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-1 h-fit w-fit cursor-pointer",
              data.tip := "Remove from Whitelist",
              onClick.foreach(_ => {
                discovery.deleteFromWhitelist(uuid)
              }),
            ),
          )
        else None,
        div(
          Icons.close("fill-red-600 w-4 h-4"),
          cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
          data.tip := "Close Connection",
          onClick.foreach(_ => {
            ref.disconnect()
          }),
        ),
      ),
    )
  } else
    div(
      cls := "flex items-center justify-between p-2 hover:bg-slate-100 rounded-md",
      div(
        cls := "flex flex-col text-sm",
        div(
          name,
          cls := "font-bold",
        ),
        i(
          span(
            "Source: ",
            cls := "text-slate-400",
          ),
          source,
          cls := "text-slate-500 text-xs",
        ),
        i(
          span(
            "Connection: ",
            cls := "text-slate-400",
          ),
          Signals.fromFuture(webrtc.getConnectionMode(ref)),
          cls := "text-slate-500 text-xs",
        ),
      ),
      div(
        Icons.close("fill-red-600 w-4 h-4"),
        cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
        data.tip := "Close Connection",
        onClick.foreach(_ => ref.disconnect()),
      ),
    )
}

def availableConnectionRow(
    connection: AvailableConnection,
)(using discovery: DiscoveryService, webrtc: WebRTCService) = {
  div(
    cls := "flex items-center justify-between p-2 hover:bg-slate-100 rounded-md",
    div(
      cls := "flex flex-col text-sm",
      div(
        connection.name,
        cls := "font-bold",
      ),
      i(
        span(
          "ID: ",
          cls := "text-slate-400",
        ),
        connection.uuid.split("-")(0),
        cls := "text-slate-500 text-xs",
      ),
      i(
        span(
          "Trust: ",
          cls := "text-slate-400",
        ),
        if (connection.trusted && !connection.mutualTrust) "wait for trust" else "no trust",
        cls := "text-slate-500 text-xs",
      ),
    ),
    div(
      Icons.check("w-4 h-4", "stroke-green-600"),
      cls := "tooltip tooltip-left hover:bg-green-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
      data.tip := "Add to Whitelist",
      onClick.foreach(_ => discovery.addToWhitelist(connection.uuid)),
    ),
  )
}