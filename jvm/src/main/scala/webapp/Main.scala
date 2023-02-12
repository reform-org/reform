package webapp

import loci.registry.Registry
import loci.communicator.ws.jetty.WS
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import webapp.npm.SqliteDB

@main def runServer() = {
  val registry = Registry()
  val indexedDb = SqliteDB()
  val _ = Repositories(using registry, indexedDb)

  val server = new Server()
  val connector = new ServerConnector(server)
  connector.setPort(1334)
  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  server.setHandler(context)
  server.addConnector(connector)
  registry.listen(WS(context, "/registry/*")).get
  server.start()
  println("listening on ws://localhost:1334/registry/")
  server.join()
}
