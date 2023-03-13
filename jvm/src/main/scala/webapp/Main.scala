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
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import org.eclipse.jetty.security.ServerAuthException
import org.eclipse.jetty.server.UserIdentity.Scope
import java.security.Principal
import javax.security.auth.Subject

// https://github.com/eclipse/jetty.project/blob/jetty-11.0.14/jetty-security/src/main/java/org/eclipse/jetty/security/authentication/BasicAuthenticator.java#L50
@main def runServer() = {
  val registry = Registry()
  val indexedDb = SqliteDB()
  val _ = Repositories()(using registry, indexedDb)

  val server = new Server()
  val connector = new ServerConnector(server)
  val port = sys.env.get("VITE_ALWAYS_ONLINE_PEER_LISTEN_PORT").get.toInt
  val path = sys.env.get("VITE_ALWAYS_ONLINE_PEER_PATH").get
  val secret = sys.env.get("JWT_KEY").get
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
        try {
          val algorithm: Algorithm = Algorithm.HMAC256(secret).nn;
          val verifier: JWTVerifier = JWT
            .require(algorithm)
            .nn
            .build()
            .nn;

          val decodedJWT: DecodedJWT = verifier.verify(token).nn;
          val issuedAt = decodedJWT.getIssuedAt();
          val expiresAt = decodedJWT.getExpiresAt();
          val uuid = decodedJWT.getClaim("uuid").nn.asString();
          val device = decodedJWT.getClaim("device").nn.asString();
          val username = decodedJWT.getClaim("username").nn.asString();
          println(
            s"connection from ${username} (${uuid}) from device ${device} issued ${issuedAt} expiring ${expiresAt}",
          )
          return new Authentication.User {

            override def logout(request: ServletRequest | Null): Authentication | Null = ???

            override def getAuthMethod(): String | Null = null

            override def getUserIdentity(): UserIdentity | Null = new UserIdentity {

              override def getSubject(): Subject | Null = ???

              override def getUserPrincipal(): Principal | Null = new Principal {

                override def getName(): String | Null = "test"

              }

              override def isUserInRole(role: String | Null, scope: Scope | Null): Boolean = true

            }

            override def isUserInRole(scope: UserIdentity.Scope | Null, role: String | Null): Boolean = true

          }
        } catch {
          case exception: JWTVerificationException =>
            println(exception)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return Authentication.SEND_FAILURE
        }
      } else {
        println("not authenticated")
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return Authentication.SEND_FAILURE
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
  registry.listen(WS(servletContextHandler, s"$path")).get
  server.start()
  println(s"listening on ws://localhost:$port$path")
  server.join()
}
