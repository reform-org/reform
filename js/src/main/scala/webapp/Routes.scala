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

import colibri.*
import colibri.router.*
import colibri.router.Router
import org.scalajs.dom.window
import outwatch.*
import rescala.default.*
import webapp.pages.*
import webapp.services.Page

object Routes {
  val fromPath: Path => Page = {
    case Root              => HomePage()
    case Root / "login"    => LoginPage()
    case Root / "projects" => ProjectsPage()
    case Root / "users"    => UsersPage()
    case Root / "hiwis"    => HiwisPage()
    case Root / "webrtc"   => WebRTCHandling();
  }

  val toPath: Page => Path = {
    case HomePage()       => Root / ""
    case LoginPage()      => Root / "login"
    case ProjectsPage()   => Root / "projects"
    case UsersPage()      => Root / "users"
    case HiwisPage()       => Root / "hiwis"
    case WebRTCHandling() => Root / "webrtc";
  }
}
