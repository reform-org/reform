package webapp.repo

import rescala.default.*
import scala.concurrent.Future

// TODO: make members private and create accessors
case class Synced[A](storage: Storage[A], id: String, signal: Signal[A], private val outgoingDeltaEvent: Evt[A => A]) {

  def update(f: A => A): Future[Unit] = {
    //storage.set(id, f())
    outgoingDeltaEvent.fire(f)
  }

}
