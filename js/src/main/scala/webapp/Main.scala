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

import cats.effect.SyncIO
import loci.registry.Registry
import outwatch.*
import outwatch.dsl.*
import webapp.npm.IIndexedDB
import webapp.npm.IndexedDB
import webapp.services.DiscoveryService
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.services.*
import webapp.BasicCodecs.*
import webapp.utils.Futures.*
import webapp.given_ExecutionContext

import scala.scalajs.js
import scala.annotation.nowarn

object Main {
  def main(): Unit = {
    lazy val toaster: Toaster = Toaster()
    lazy val mailing: MailService = MailService()
    lazy val routing: RoutingService = RoutingService(using jsImplicits)
    lazy val indexedDb: IIndexedDB = IndexedDB(using jsImplicits)
    lazy val registry: Registry = Registry()
    lazy val webrtc: WebRTCService = WebRTCService(using registry, toaster, discovery)
    lazy val repositories: Repositories = Repositories()(using registry, indexedDb)
    lazy val discovery: DiscoveryService = DiscoveryService(using toaster)
    lazy val jsImplicits: JSImplicits =
      JSImplicits(toaster, mailing, routing, indexedDb, registry, webrtc, repositories, discovery)
    // we could assign the members later if this doesn't work?

    helpers.OutwatchTracing.error.unsafeForeach { throwable =>
      toaster.make(
        s"Unknown internal exception: ${throwable.getMessage}",
        ToastMode.Infinit,
        ToastType.Error,
      )
    }

    indexedDb
      .update[String]("test", _ => "test")
      .onComplete(value => {
        if (value.isFailure) {
          toaster.make(
            "Application unusable because storage is not available. Your Browser does not support IndexedDB! Private tabs in Firefox don't work.",
            ToastMode.Persistent,
            ToastType.Error,
          )
        }
      })

    if (discovery.tokenIsValid(discovery.token.now))
      discovery
        .connect(using jsImplicits)()
        .toastOnError(using jsImplicits)()
    Outwatch
      .renderInto[SyncIO](
        "#app",
        body(
          routing.render,
          toaster.render,
        ),
      )
      .unsafeRunSync()
  }
}

val _ = Main.main()
