package webapp.components

import loci.transmitter.RemoteRef
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.given
import webapp.services.AvailableConnection
import webapp.services.DiscoveryService
import webapp.webrtc.WebRTCService

import webapp.given_ExecutionContext

def connectionRow(name: String, source: String, uuid: String, ref: RemoteRef)(using
    webrtc: WebRTCService,
    discovery: DiscoveryService,
) = {
  if (source == "discovery") {
    val own = discovery.decodeToken(discovery.token.now.get)
    div(
      cls := "flex items-center justify-between p-2 hover:bg-slate-100 dark:hover:bg-gray-600 rounded-md",
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
          uuid.split("-").nn(0).nn,
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
              icons.Forbidden(cls := "text-red-600 w-3 h-3"),
              cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-1 h-fit w-fit cursor-pointer",
              data.tip := "Remove from Whitelist",
              onClick.foreach(_ => {
                discovery.deleteFromWhitelist(uuid)
              }),
            ),
          )
        else None,
        div(
          icons.Close(cls := "text-red-600 w-4 h-4"),
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
        icons.Close(cls := "text-red-600 w-4 h-4"),
        cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
        data.tip := "Close Connection",
        onClick.foreach(_ => ref.disconnect()),
      ),
    )
}

def availableConnectionRow(
    connection: AvailableConnection,
)(using discovery: DiscoveryService) = {
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
        connection.uuid.split("-").nn(0).nn,
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
      icons.Check(cls := "w-4 h-4 text-green-600"),
      cls := "tooltip tooltip-left hover:bg-green-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
      data.tip := "Add to Whitelist",
      onClick.foreach(_ => discovery.addToWhitelist(connection.uuid)),
    ),
  )
}
