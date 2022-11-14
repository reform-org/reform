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
package webapp

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO

import colibri.Subject

// Outwatch documentation:
// https://outwatch.github.io/docs/readme.html

object Main {
  def main(args: Array[String]): Unit =
    Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

  def app = div(
    h1("Hello World!"),
    counter,
    inputField,
  )

  def counter = SyncIO {
    // https://outwatch.github.io/docs/readme.html#example-counter
    val number = Subject.behavior(0)
    div(
      button("+", onClick(number.map(_ + 1)) --> number, marginRight := "10px"),
      number,
    )
  }

  def inputField = SyncIO {
    // https://outwatch.github.io/docs/readme.html#example-input-field
    val text = Subject.behavior("")
    div(
      input(
        tpe := "text",
        value <-- text,
        onInput.value --> text,
      ),
      button("clear", onClick.as("") --> text),
      div("text: ", text),
      div("length: ", text.map(_.length)),
    )
  }

}
