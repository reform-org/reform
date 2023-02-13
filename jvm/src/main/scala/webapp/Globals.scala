package webapp

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors

given ExecutionContext =
  ExecutionContext.fromExecutor(Executors.newSingleThreadScheduledExecutor())
