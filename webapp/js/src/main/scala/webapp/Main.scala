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
package de.tu_darmstadt.informatik.st.reform

import cats.effect.SyncIO
import de.tu_darmstadt.informatik.st.reform.BasicCodecs.*
import de.tu_darmstadt.informatik.st.reform.components.navigationHeader
import de.tu_darmstadt.informatik.st.reform.given_ExecutionContext
import de.tu_darmstadt.informatik.st.reform.npm.IIndexedDB
import de.tu_darmstadt.informatik.st.reform.npm.IndexedDB
import de.tu_darmstadt.informatik.st.reform.services.DiscoveryService
import de.tu_darmstadt.informatik.st.reform.services.RoutingService
import de.tu_darmstadt.informatik.st.reform.services.*
import de.tu_darmstadt.informatik.st.reform.utils.Futures.*
import de.tu_darmstadt.informatik.st.reform.webrtc.WebRTCService
import loci.registry.Registry
import outwatch.*
import outwatch.dsl.*

object Main {
  def main(): Unit = {
    println("Now just fix the fucking scala code, baka Shinji")
    lazy val jsImplicits: JSImplicits =
      new JSImplicits() {
        lazy val toaster: Toaster = Toaster()
        lazy val mailing: MailService = MailService()
        lazy val routing: RoutingService = RoutingService(using jsImplicits)
        lazy val indexeddb: IIndexedDB = IndexedDB(using jsImplicits)
        lazy val registry: Registry = Registry()
        lazy val webrtc: WebRTCService = WebRTCService(using registry, toaster, discovery)
        lazy val repositories: Repositories = Repositories()(using registry, indexeddb)
        lazy val discovery: DiscoveryService = DiscoveryService()
      }
    // we could assign the members later if this doesn't work?

    helpers.OutwatchTracing.error.unsafeForeach { throwable =>
      jsImplicits.toaster.make(
        s"Unknown internal exception: ${throwable.getMessage}",
        ToastMode.Infinit,
        ToastType.Error,
      )
    }

    jsImplicits.indexeddb
      .update[String]("test", _ => "test")
      .onComplete(value => {
        if (value.isFailure) {
          jsImplicits.toaster.make(
            "Application unusable because storage is not available. Your Browser does not support IndexedDB! Private tabs in Firefox don't work.",
            ToastMode.Persistent,
            ToastType.Error,
          )
        }
      })

    if (jsImplicits.discovery.tokenIsValid(jsImplicits.discovery.token.now))
      jsImplicits.discovery
        .connect(using jsImplicits)()
        .toastOnError(using jsImplicits)()
    Outwatch
      .renderReplace[SyncIO](
        "#app",
        render(using jsImplicits),
      )
      .unsafeRunSync()
  }

  def render(using jsImplicits: JSImplicits): VNode = div(
    navigationHeader(jsImplicits.routing.render),
    jsImplicits.toaster.render,
  )
}

private val _ = Main.main()
