package de.tu_darmstadt.informatik.st.reform

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

given ExecutionContext =
  ExecutionContext.fromExecutor(Executors.newSingleThreadScheduledExecutor().nn)

// https://github.com/scala-loci/scala-loci/blob/master/communication/shared/src/main/scala/loci/logging/package.scala
// https://github.com/outr/scribe/wiki/Features
// val _ = Logger("scala-loci").clearHandlers().replace()

object Globals {
  val VITE_DATABASE_VERSION: String =
    sys.env("VITE_DATABASE_VERSION").nn

  val VITE_PROTOCOL_VERSION: String =
    sys.env("VITE_PROTOCOL_VERSION").nn
}
