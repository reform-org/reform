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
import de.tu_darmstadt.informatik.st.reform.Globals

import java.sql.*
import scala.concurrent.Future

class SqliteDB(dbPath: String) extends IIndexedDB {
  // This fishy line makes it so, that sqlite is actually found
  // See: https://stackoverflow.com/a/16725406
  Class.forName("org.sqlite.JDBC")

  val url = s"jdbc:sqlite:$dbPath"

  private val connection: Connection = DriverManager.getConnection(url).nn
  connection.setAutoCommit(false)
  val _ = connection.createStatement.nn.execute(
    s"CREATE TABLE IF NOT EXISTS reform_${Globals.VITE_DATABASE_VERSION} (key TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL);",
  )
  connection.commit()

  private val readStatement: PreparedStatement =
    connection.prepareStatement(s"SELECT value FROM reform_${Globals.VITE_DATABASE_VERSION} WHERE key = ?;").nn

  private val writeStatement: PreparedStatement =
    connection
      .prepareStatement(
        s"INSERT INTO reform_${Globals.VITE_DATABASE_VERSION} (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET value = excluded.value;",
      )
      .nn

  def requestPersistentStorage(): Unit = {}

  override def get[T](key: String)(using codec: JsonValueCodec[T]): Future[Option[T]] = {
    synchronized {
      val dbValue = readValue(key)
      connection.commit()
      val o = dbValue.map(readFromString(_))
      Future.successful(o)
    }
  }

  override def update[T](key: String, fun: Option[T] => T)(using codec: JsonValueCodec[T]): Future[T] = {
    synchronized {
      val dbValue = readValue(key)
      val value = fun(dbValue.map(readFromString(_)))
      writeStatement.setString(1, key)
      writeStatement.setString(2, writeToString(value))
      val _ = writeStatement.execute()
      connection.commit()
      Future.successful(value)
    }
  }

  private def readValue(key: String): Option[String] = {
    readStatement.setString(1, key)
    val resultSet = readStatement.executeQuery().nn
    if (resultSet.next()) {
      Some(resultSet.getString("value").nn)
    } else {
      None
    }
  }
}
