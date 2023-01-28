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

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.js.JSConverters.*

import com.github.plokhotnyuk.jsoniter_scala.core.*

class IndexedDB extends IIndexedDB {

  private val data: mutable.Map[String, Any] = mutable.Map()

  override def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]] = {
    val o = data.get(key).map(_.asInstanceOf[T])
    Future.successful(o)
  }

  override def set[T](key: String, value: T)(using codec: JsonValueCodec[T]): Future[Unit] = {
    data.put(key, value)
    Future.successful(())
  }
}
