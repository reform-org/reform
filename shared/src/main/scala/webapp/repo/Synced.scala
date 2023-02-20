package webapp.repo

import rescala.default.*
import scala.concurrent.Future
import webapp.given_ExecutionContext

case class Synced[A](private val storage: Storage[A], id: String, private val value: Var[A]) {

  val editingValue: Var[Option[A]] = Var(None)

  // TODO FIXME remove Option here?
  def update(f: Option[A] => A): Future[A] = {
    storage
      .update(id, f)
      .map(newValue => {
        // TODO FIXME this is prone to race conditions with the storage.update
        // maybe we could do this update within the databae transation?
        value.set(newValue)
        newValue
      })
  }

  val signal: Signal[A] = value
}
