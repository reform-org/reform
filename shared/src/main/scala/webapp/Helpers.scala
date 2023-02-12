package webapp

import rescala.core.Disconnectable
import scala.annotation.nowarn

// TODO FIXME these should not be ignored
def ignoreDisconnectable(@nowarn("msg=unused explicit parameter") disconnectable: Disconnectable): Unit = {}
