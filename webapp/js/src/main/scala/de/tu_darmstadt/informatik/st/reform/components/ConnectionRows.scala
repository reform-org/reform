package de.tu_darmstadt.informatik.st.reform.components

import de.tu_darmstadt.informatik.st.reform.JSImplicits
import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.given
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.services.AvailableConnection
import loci.transmitter.RemoteRef
import outwatch.*
import outwatch.dsl.*
import rescala.default.*

def connectionRow(name: String, source: String, uuid: String, displayId: String, ref: RemoteRef)(using
    jsImplicits: JSImplicits,
) = {
  if (source == "discovery") {
    val own = jsImplicits.discovery.decodeToken(jsImplicits.discovery.token.now.get)
    div(
      cls := "flex items-center justify-between p-2 hover:bg-slate-100 dark:hover:bg-gray-700/50 rounded-md",
      div(
        cls := "flex flex-col text-sm",
        div(
          name,
          cls := "font-bold",
        ),
        i(
          span(
            "ID: ",
            cls := "text-slate-400 dark:text-gray-400",
          ),
          displayId,
          cls := "text-slate-500 text-xs dark:text-gray-200",
        ),
        i(
          span(
            "Source: ",
            cls := "text-slate-400 dark:text-gray-400",
          ),
          source,
          cls := "text-slate-500 text-xs dark:text-gray-200",
        ),
        i(
          span(
            "Connection: ",
            cls := "text-slate-400 dark:text-gray-400",
          ),
          Signal.fromFuture(jsImplicits.webrtc.getConnectionMode(ref)),
          cls := "text-slate-500 text-xs dark:text-gray-200",
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
                jsImplicits.discovery.deleteFromWhitelist(uuid)
              }),
            ),
          )
        else None,
        div(
          icons.Close(cls := "text-red-600 w-4 h-4"),
          cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
          data.tip := "Close Connection",
          onClick.foreach(_ => {
            jsImplicits.discovery.disconnect(ref)
          }),
        ),
      ),
    )
  } else
    div(
      cls := "flex items-center justify-between p-2 hover:bg-slate-100 dark:hover:bg-gray-700/50 rounded-md",
      div(
        cls := "flex flex-col text-sm",
        div(
          name,
          cls := "font-bold",
        ),
        i(
          span(
            "Source: ",
            cls := "text-slate-400 dark:text-gray-400",
          ),
          source,
          cls := "text-slate-500 text-xs dark:text-gray-200",
        ),
        i(
          span(
            "Connection: ",
            cls := "text-slate-400 dark:text-gray-400",
          ),
          Signal.fromFuture(jsImplicits.webrtc.getConnectionMode(ref)),
          cls := "text-slate-500 text-xs dark:text-gray-200",
        ),
      ),
      div(
        icons.Close(cls := "text-red-600 w-4 h-4"),
        cls := "tooltip tooltip-left hover:bg-red-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
        data.tip := "Close Connection",
        onClick.foreach(_ => jsImplicits.discovery.disconnect(ref)),
      ),
    )
}

def availableConnectionRow(
    connection: AvailableConnection,
)(using jsImplicits: JSImplicits) = {
  div(
    cls := "flex items-center justify-between p-2 hover:bg-slate-100 dark:hover:bg-gray-700/50 rounded-md mt-2",
    div(
      cls := "flex flex-col text-sm",
      div(
        connection.name,
        cls := "font-bold",
      ),
      i(
        span(
          "ID: ",
          cls := "text-slate-400 dark:text-gray-400",
        ),
        connection.displayId,
        cls := "text-slate-500 text-xs dark:text-gray-200",
      ),
      i(
        span(
          "Trust: ",
          cls := "text-slate-400 dark:text-gray-400",
        ),
        if (connection.trusted && !connection.mutualTrust) s"wait for ${connection.name} to trust you"
        else if (!connection.trusted) s"you do not trust ${connection.name} "
        else "you trust each other",
        cls := "text-slate-500 text-xs dark:text-gray-200",
      ),
    ),
    if (!connection.trusted) {
      div(
        icons.Check(cls := "w-4 h-4 text-green-600"),
        cls := "tooltip tooltip-left hover:bg-green-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
        data.tip := "Add to Whitelist",
        onClick.foreach(_ => jsImplicits.discovery.addToWhitelist(connection.uuid)),
      )
    } else None,
    if (connection.trusted && connection.mutualTrust) {
      div(
        icons.Check(cls := "w-4 h-4 text-green-600"),
        cls := "tooltip tooltip-left hover:bg-green-200 rounded-md p-0.5 h-fit w-fit cursor-pointer",
        data.tip := "Connect",
        onClick.foreach(_ => jsImplicits.discovery.connectTo(connection.uuid)),
      )
    } else None,
  )
}
