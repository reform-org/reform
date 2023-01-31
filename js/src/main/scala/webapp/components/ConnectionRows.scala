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

import webapp.services.DiscoveryService.AvailableConnection
import webapp.webrtc.WebRTCService

def connectionRow(name: String, source: String, uuid: String, ref: RemoteRef)(using services: Services) = {
  if (source == "discovery")
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
          Signals.fromFuture(getConnectionMode(ref)),
          cls := "text-slate-500 text-xs",
        ),
      ),
      div(
        cls := "flex flex-row gap-1",
        div(
          Icons.forbidden("fill-red-600 w-3 h-3"),
          cls := "hover:bg-red-200 rounded-md p-1 h-fit w-fit cursor-pointer",
          onClick.foreach(_ => services.discovery.deleteFromWhitelist(uuid)),
        ),
        div(
          Icons.close("fill-red-600 w-4 h-4"),
          cls := "hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
          onClick.foreach(_ => {
            ref.disconnect()
          }),
        ),
      ),
    )
  else
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
          Signals.fromFuture(getConnectionMode(ref)),
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

def availableConnectionRow(connection: AvailableConnection)(using services: Services) = {
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
      cls := "hover:bg-green-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
      onClick.foreach(_ => services.discovery.addToWhitelist(connection.uuid)),
    ),
  )
}
