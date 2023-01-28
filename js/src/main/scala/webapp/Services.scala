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
package webapp

import webapp.services.RoutingService
import webapp.webrtc.WebRTCService

trait Services {
  val routing: RoutingService
  val webrtc: WebRTCService.type
}

object ServicesDefault extends Services {
  val routing = RoutingService()
  val webrtc = WebRTCService
  val repositories = Repositories
}
