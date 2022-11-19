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

import colibri._
import colibri.router._
import colibri.router.Router
import org.scalajs.dom.window
import outwatch._
import rescala.default._
import webapp.pages._
import webapp.services._

object Routes:
  val fromPath: Path => Page =
    case Root                  => HomePage()
    case Root / "login"        => LoginPage()
    case Root / "project" / id => ProjectPage(id)
    case Root / "webrtc"       => WebRTCHandling();

  val toPath: Page => Path =
    case HomePage()       => Root / ""
    case LoginPage()      => Root / "login"
    case ProjectPage(id)  => Root / "project" / id
    case WebRTCHandling() => Root / "webrtc";
