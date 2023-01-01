/*
Copyright 2022 https://github.com/phisn/ratable, The reform-org/reform contributors

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
package webapp.services

import webapp.*

import java.util.concurrent.ThreadLocalRandom

import kofre.decompose.interfaces.MVRegisterInterface.MVRegisterSyntax
import kofre.decompose.containers.DeltaBufferRDT
import kofre.datatypes.TimedVal
import kofre.datatypes.PosNegCounter
import kofre.base.{Bottom, DecomposeLattice}

import loci.registry.{Binding, Registry}
import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import loci.transmitter.RemoteRef
import loci.serializer.jsoniterScala.given

import org.scalajs.dom.html.{Input, LI}
import org.scalajs.dom.*
import org.scalajs.dom

import rescala.default.*

import scribe.Execution.global

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.Function.const
import scala.collection.mutable
import scala.scalajs.js.timers.setTimeout
import scala.util.{Failure, Success}
import scala.reflect.Selectable.*
import scala.scalajs.js
import loci.serializer.jsoniterScala.given
import kofre.datatypes.PosNegCounter
import scala.scalajs.js.JSON

object WebRTCService {
  val registry = new Registry

  val projectBinding = Binding[DeltaFor[Project] => Unit]("project")
  val projectReplicator = ReplicationGroup(rescala.default, WebRTCService.registry, projectBinding)

  val projectsBinding = Binding[DeltaFor[GrowOnlySet[String]] => Unit]("projects")
  val projectsReplicator = ReplicationGroup(rescala.default, WebRTCService.registry, projectsBinding)

  val userBinding = Binding[DeltaFor[User] => Unit]("user")
  val userReplicator = ReplicationGroup(rescala.default, WebRTCService.registry, userBinding)

  val usersBinding = Binding[DeltaFor[GrowOnlySet[String]] => Unit]("users")
  val usersReplicator = ReplicationGroup(rescala.default, WebRTCService.registry, usersBinding)

  val counterBinding = Binding[DeltaFor[PosNegCounter] => Unit]("counter")
  val counterReplicator = ReplicationGroup(rescala.default, WebRTCService.registry, counterBinding)
}
