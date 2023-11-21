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
}
