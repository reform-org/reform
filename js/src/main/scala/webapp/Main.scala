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
    given toaster: Toaster = Toaster()
    given routing: RoutingService = RoutingService()
    given indexedDb: IIndexedDB = IndexedDB()
    given registry: Registry = Registry()
    given webrtc: WebRTCService = WebRTCService()
    given repositories: Repositories = Repositories()
    given discovery: DiscoveryService = DiscoveryService()

    helpers.OutwatchTracing.error.unsafeForeach { throwable =>
      toaster.make(
        s"Unknown internal exception: ${throwable.getMessage}",
        ToastMode.Infinit,
        ToastType.Error,
      )
    }: @nowarn

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
        .connect()
        .toastOnError()
    Outwatch.renderInto[SyncIO]("#app", app()).unsafeRunSync()
  }

  private def app(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ) = body(
    routing.render,
    toaster.render,
  )
}

val _ = Main.main()
