package webapp.repo

import rescala.default.*
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

// TODO: make members private and create accessors
case class Synced[A](storage: Storage[A], id: String, value: Var[A]) {

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
}
