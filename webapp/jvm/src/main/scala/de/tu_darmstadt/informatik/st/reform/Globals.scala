package de.tu_darmstadt.informatik.st.reform

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import scribe.Logger
import java.util.Properties
import java.io.File
import scala.io.Source
import java.io.FileNotFoundException
import java.net.URL
import java.net.URI

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
