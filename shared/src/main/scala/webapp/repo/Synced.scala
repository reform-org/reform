package webapp.repo

import rescala.default.*

// TODO: make members private and create accessors
case class Synced[A](id: String, signal: Signal[A], private val outgoingDeltaEvent: Evt[A => A]) {

  def update(f: A => A): Unit =
    outgoingDeltaEvent.fire(f)

}
