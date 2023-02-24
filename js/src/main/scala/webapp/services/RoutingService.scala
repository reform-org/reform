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

import colibri.*
import colibri.router.*
import org.scalajs.dom
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*

import webapp.webrtc.WebRTCService
import webapp.npm.JSUtils.cleanPopper

import scala.scalajs.js
import webapp.npm.IIndexedDB
import scala.annotation.nowarn

trait Page {
  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode
}

class RoutingService(using repositories: Repositories, toaster: Toaster, indexedb: IIndexedDB) {
  given RoutingService = this;

  private lazy val page = Var[Page](Routes.fromPath(Path(window.location.pathname)))
  private lazy val query =
    Var[Map[String, String | Seq[String]]](decodeQueryParameters(window.location.search))

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): Signal[VNode] =
    page.map(_.render)

  def to(
      newPage: Page,
      newTab: Boolean = false,
      queryParams: Map[String, String | Seq[String]] = Map(),
  ) = {
    if (newTab) {
      window.open(linkPath(newPage, queryParams), "_blank").focus();
    } else {
      window.history.pushState(null, "", linkPath(newPage, queryParams))
      cleanPopper()
      page.set(newPage)
      query.set(queryParams)
    }

    document.activeElement.asInstanceOf[HTMLElement].blur()
  }

  def decodeQueryParameters(query: String): Map[String, String | Seq[String]] = {
    var res: Map[String, String | Seq[String]] = Map()
    if (query.isBlank() || !query.startsWith("?")) return res
    query
      .substring(1)
      .split("&")
      .foreach(param => {
        if (param.contains("=")) {
          val kv = param.split("=")
          val value = if (kv.length >= 2) kv(1) else ""
          if (kv(0).matches(".*\\[\\]$")) {
            val key = kv(0).replace("[]", "")
            if (!res.contains(key)) res += (key -> Seq(value))
            else {
              val oldVal = res.get(key).getOrElse(Seq())
              oldVal match {
                case oldVal: Seq[String] => res += (key -> (oldVal :+ value))
                case _                   => {}
              }

            }
          } else {
            res += (kv(0) -> (value))
          }
        }
      })

    res
  }

  def encodeQueryParameters(map: Map[String, String | Seq[String]]) = {
    var res = ""
    if (map.size != 0) {
      var entries: Seq[String] = Seq()
      map.foreach((k, v) => {
        v match {
          case v: String      => entries = entries :+ s"$k=$v"
          case v: Seq[String] => entries = entries :+ v.map(s => s"$k[]=$s").mkString("&")
        }
      })

      res = "?" + entries.mkString("&")
    }

    res
  }

  def link(newPage: Page) =
    URL(linkPath(newPage), window.location.href).toString

  def linkPath(newPage: Page, query: Map[String, String | Seq[String]] = Map()) =
    Routes.toPath(newPage).pathString + encodeQueryParameters(query)

  def back() =
    window.history.back()

  query.map(query => {
    window.history.replaceState(null, "", linkPath(page.now, query))
  }): @nowarn

  window.onpopstate = _ => {
    page.set(Routes.fromPath(Path(window.location.pathname)))
    query.set(decodeQueryParameters(window.location.search))
  }

  query.observe(t => page.map(page => linkPath(page, t))): @nowarn
  query.observe(t => println(t)): @nowarn

}
