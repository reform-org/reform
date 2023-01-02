/*
Copyright 2022 The reform-org/reform contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package webapp.pages

import org.scalajs.dom
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.*
import webapp.given
import webapp.components.navigationHeader

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.*
import cats.effect.SyncIO
import colibri.{Cancelable, Observer, Source, Subject}
import webapp.given
import webapp.components.navigationHeader
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.UUID

private class NewUserRow {

  private val username = Var("")
  private val role = Var("")
  private val comment = Var("")

  val onNewUser: Evt[EventedUser] = Evt[EventedUser]()

  def render(): VNode =
    tr(
      td(
        input(
          value <-- username,
          onInput.value --> username,
          placeholder := "New User Name",
        ),
      ),
      td(
        input(
          value <-- role,
          onInput.value --> role,
          placeholder := "User role",
        ),
      ),
      td(
        input(
          value <-- comment,
          onInput.value --> comment,
          placeholder := "Some comment",
        ),
      ),
      td(
        button(
          cls := "btn",
          "Create User",
          onClick.foreach(_ => addNewUser()),
        ),
      ),
    )

  private def addNewUser(): Unit = {
    try {
      val _username = validateUsername()
      val _role = validateRole()
      val _comment = validateComment()
      val user = UserService.createOrGetUser(UUID.randomUUID().toString)
      user.map(user => {
        user.changeEvent.fire(u => {
          u.withUsername(_username).withRole(_role).withComment(_comment)
        })
        onNewUser.fire(user)

        username.set("")
        role.set("")
        comment.set("")
      })
    } catch {
      case e: Exception => window.alert(e.getMessage)
    }

  }

  private def validateUsername(): String = {
    val username = this.username.now

    if (username.isBlank) {
      throw new Exception("Invalid empty Username")
    }

    username.strip
  }

  private def validateRole(): String = {
    val role = this.role.now

    if (role.isBlank) {
      throw new Exception("Invalid empty Role")
    }

    role.strip
  }

  private def validateComment(): Option[String] = {
    val comment = this.comment.now
    if (comment.isBlank) None else Some(comment)
  }
}

case class UsersPage() extends Page {

  private val newUserRow: NewUserRow = NewUserRow()

  newUserRow.onNewUser.observe(p => UsersService.users.map(_.addNewUserEvent.fire(p.id)))

  def render(using services: Services): VNode = {
    div(
      navigationHeader,
      div(
        cls := "p-1",
        h1(cls := "text-4xl text-center", "User page"),
      ),
      table(
        cls := "table-auto",
        thead(
          tr(
            th("User"),
            th("Role"),
            th("Comment"),
            th("Stuff"),
          ),
        ),
        tbody(
          UsersService.users.map(
            _.signal.map(users => renderUsers(users.set.toList.map(userId => UserService.createOrGetUser(userId)))),
          ),
          newUserRow.render(),
        ),
      ),
    )
  }

  private def renderUsers(users: List[Future[EventedUser]]): List[VNode] =
    users.map(u =>
      tr(
        td(u.map(_.signal.map(_.username))),
        td(u.map(_.signal.map(_.role))),
        td(u.map(_.signal.map(_.comment))),
        button(
          cls := "btn",
          "Delete",
          onClick.foreach(_ => u.map(removeUser)),
        ),
      ),
    )

  private def removeUser(u: EventedUser): Unit = {
    // val yes = window.confirm(s"Do you really want to delete the user \"${u.signal.now.name}\"?")
    // if (yes) {
    // ProjectsService.projects.transform(_.filterNot(_ == p))
    // }
  }

}
