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

import org.scalajs.dom.*
import scala.scalajs.js
import outwatch.*
import outwatch.dsl.*
import webapp.{*, given}
import webapp.components.navigationHeader
import webapp.npm.*
import webapp.services.DiscoveryService
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService

import webapp.components.common.*

import webapp.services.{ToastMode, ToastType, Toaster}
import webapp.given_ExecutionContext
import webapp.components.{Modal, ModalButton}
import webapp.utils.Futures.*
import webapp.utils.{exportIndexedDBJson, importIndexedDBJson}
import webapp.npm.JSUtils.downloadFile
import org.scalajs.dom.HTMLInputElement
import rescala.default.*

import scala.util.Success
import scala.util.Failure
import webapp.services.MailService
import webapp.JSImplicits

case class HomePage()(using
    jsImplicits: JSImplicits,
) extends Page {

  def render: VNode = {

    navigationHeader(
      div(
        cls := "flex flex-col gap-2 max-w-sm",
      ),
    )
  }
}
