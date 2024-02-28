package de.tu_darmstadt.informatik.st.reform

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

given ExecutionContext =
  ExecutionContext.fromExecutor(Executors.newSingleThreadScheduledExecutor().nn)

// https://github.com/scala-loci/scala-loci/blob/master/communication/shared/src/main/scala/loci/logging/package.scala
// https://github.com/outr/scribe/wiki/Features
// val _ = Logger("scala-loci").clearHandlers().replace()

object Env {
  def get(name: String): String = {
    val opt = sys.env.get(name)
    if (opt.isEmpty) {
      throw new IllegalStateException(s"Environment variable ${name} must be set. (Did you source .env)?")
    }
    opt.get
  }
}

object Globals {
  val VITE_DATABASE_VERSION: String = Env.get("VITE_DATABASE_VERSION")

  val VITE_PROTOCOL_VERSION: String = Env.get("VITE_PROTOCOL_VERSION")

  val ALWAYS_ONLINE_PEER_DATABASE_PATH: String = Env.get("ALWAYS_ONLINE_PEER_DATABASE_PATH")

  val VITE_ALWAYS_ONLINE_PEER_PATH: String = Env.get("VITE_ALWAYS_ONLINE_PEER_PATH")

  val VITE_ALWAYS_ONLINE_PEER_LISTEN_PORT: Int = Env.get("VITE_ALWAYS_ONLINE_PEER_LISTEN_PORT").toInt

  val JWT_KEY: String = Env.get("JWT_KEY")
}
