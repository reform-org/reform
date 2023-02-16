package webapp

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import scribe.Logger

given ExecutionContext =
  ExecutionContext.fromExecutor(Executors.newSingleThreadScheduledExecutor())

// https://github.com/scala-loci/scala-loci/blob/master/communication/shared/src/main/scala/loci/logging/package.scala
// https://github.com/outr/scribe/wiki/Features
val _ = Logger("scala-loci").clearHandlers().replace()
