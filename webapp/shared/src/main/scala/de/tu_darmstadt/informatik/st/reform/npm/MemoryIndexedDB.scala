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
package de.tu_darmstadt.informatik.st.reform.npm

import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.concurrent.Future

class MemoryIndexedDB extends IIndexedDB {

  private var data: Map[String, String] = Map()

  def requestPersistentStorage(): Unit = {}

  override def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]] = {
    val o = data.get(key).map(readFromString(_))
    Future.successful(o)
  }

  override def update[T](key: String, fun: Option[T] => T)(using codec: JsonValueCodec[T]): Future[T] = {
    val value = fun(data.get(key).map(readFromString(_)))
    data += (key -> writeToString(value))
    Future.successful(value)
  }
}
