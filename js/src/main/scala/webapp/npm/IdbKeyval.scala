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
package webapp.npm

import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js
import scala.scalajs.js.Promise

// https://github.com/jakearchibald/idb-keyval/blob/main/src/index.ts#L44
@js.native
@JSImport("idb-keyval", JSImport.Namespace)
object IdbKeyval extends js.Object {

  // TODO FIXME key type is wrong
  def get[T](key: String): Promise[js.UndefOr[T]] = js.native

  def set(key: String, value: scala.scalajs.js.Dynamic): Promise[Unit] = js.native
}