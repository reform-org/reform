package webapp

import loci.registry.Registry
import loci.communicator.ws.jetty.WS
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import webapp.npm.SqliteDB
import org.eclipse.jetty.security.SecurityHandler
import org.eclipse.jetty.security.ConstraintSecurityHandler
import org.eclipse.jetty.security.Authenticator
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.eclipse.jetty.server.Authentication
import org.eclipse.jetty.server.UserIdentity
import org.eclipse.jetty.security.authentication.LoginAuthenticator
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.security.AbstractLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.security.IdentityService
import org.eclipse.jetty.security.Authenticator.AuthConfiguration
import java.util as ju

// https://github.com/eclipse/jetty.project/issues/4123
// https://github.com/eclipse/jetty.project/blob/jetty-11.0.14/jetty-security/src/main/java/org/eclipse/jetty/security/authentication/BasicAuthenticator.java
@main def runServer() = {
  val registry = Registry()
  val indexedDb = SqliteDB()
  val _ = Repositories()(using registry, indexedDb)

  val server = new Server()
  val connector = new ServerConnector(server)
  val port = sys.env.get("VITE_ALWAYS_ONLINE_PEER_PORT").getOrElse("1334").toInt
  connector.setPort(port)
  val servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY)
  val securityHandler = servletContextHandler.getSecurityHandler().nn

  val authenticator = new Authenticator {

    override def prepareRequest(request: ServletRequest | Null): Unit = {}

    override def setConfiguration(configuration: AuthConfiguration | Null): Unit = {}

    override def getAuthMethod(): String | Null = "JWT Authenticator"

    override def validateRequest(
        req: ServletRequest | Null,
        res: ServletResponse | Null,
        mandatory: Boolean,
    ): Authentication | Null = {
      val request: HttpServletRequest = req.asInstanceOf[HttpServletRequest];
      val response: HttpServletResponse = res.asInstanceOf[HttpServletResponse];
      val token = request.getQueryString()

      if (token != null) {
        println(token)
        return new Authentication.User {

          override def logout(request: ServletRequest | Null): Authentication | Null = null

          override def getAuthMethod(): String | Null = null

          override def getUserIdentity(): UserIdentity | Null = null

          override def isUserInRole(scope: UserIdentity.Scope | Null, role: String | Null): Boolean = true

        }
      } else {
        println("not authenticated")
        return new Authentication.ResponseSent {}
      }
    }

    override def secureResponse(
        request: ServletRequest | Null,
        response: ServletResponse | Null,
        mandatory: Boolean,
        validatedUser: Authentication.User | Null,
    ): Boolean = true
  }
  securityHandler.setAuthenticator(authenticator);
  server.setHandler(servletContextHandler)
  server.addConnector(connector)
  val listener = WS(servletContextHandler, "/registry/*")
  registry.listen(listener).get
  server.start()
  println(s"listening on ws://localhost:$port/registry/")
  server.join()
}
