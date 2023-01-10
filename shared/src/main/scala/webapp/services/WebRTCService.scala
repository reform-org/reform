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

import kofre.datatypes.PosNegCounter
import loci.registry.{Binding, Registry}
import webapp.*
import webapp.repo.Repository
import webapp.Codecs.*
import rescala.default.*
import loci.serializer.jsoniterScala.given

object WebRTCService {
  val registry = new Registry

  val projectRepo: Repository[Project] = Repository("project", Project.empty)
  val userRepo: Repository[User] = Repository("user", User.empty)

  val counterBinding = Binding[DeltaFor[PosNegCounter] => Unit]("counter")
  val counterReplicator = ReplicationGroup(rescala.default, WebRTCService.registry, counterBinding)
}
