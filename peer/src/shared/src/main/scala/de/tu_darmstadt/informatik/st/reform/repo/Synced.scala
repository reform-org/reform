package de.tu_darmstadt.informatik.st.reform.repo

import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import rescala.default.*

import scala.concurrent.Future

case class Synced[A](private val storage: Storage[A], id: String, private val value: Var[A]) {

  // TODO FIXME remove Option here?
  def update(f: Option[A] => A): Future[A] = {
    storage
      .update(id, f)
      .map(newValue => {
        // TODO FIXME this is prone to race conditions with the storage.update
        // maybe we could do this update within the database translation?
        value.set(newValue)
        newValue
      })
  }

  val signal: Signal[A] = value
}
