package webapp

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
val _ = Logger("scala-loci").clearHandlers().replace()

object Globals {
  val properties = {
    val properties: Properties = new Properties()
    val source = Source.fromFile(File("../.env"))
    properties.load(source.bufferedReader())
    properties
  }

  val VITE_DATABASE_VERSION: String = sys.env.get("VITE_DATABASE_VERSION").get

  val VITE_PROTOCOL_VERSION: String = sys.env.get("VITE_PROTOCOL_VERSION").get
}
