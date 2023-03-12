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
import scalajs.js.JSConverters.JSRichOption

trait Page {
  def render: VNode
}

class RoutingService(using
    repositories: Repositories,
    toaster: Toaster,
    indexedb: IIndexedDB,
    mailing: MailService,
    webrtc: WebRTCService,
    discovery: DiscoveryService,
) {
  given RoutingService = this;

  private lazy val page = Var[Page](Routes.fromPath(Path(window.location.pathname)))
  private lazy val query =
    Var[Map[String, String | Seq[String]]](decodeQueryParameters(window.location.search))

  val queryParameters: Signal[Map[String, String | Seq[String]]] = query.map(identity)

  def render: Signal[VNode] =
    page.map(_.render)

  def to(
      newPage: Page,
      newTab: Boolean = false,
      queryParams: Map[String, String | Seq[String]] = Map(),
      keepFocus: Boolean = false,
  ) = {
    if (newTab) {
      window.open(linkPath(newPage, queryParams), "_blank").focus();
    } else {
      window.history.pushState(null.asInstanceOf[js.Any], "", linkPath(newPage, queryParams))
      cleanPopper()
      page.set(newPage)
      query.set(queryParams)
    }

    if (!keepFocus) {
      document.activeElement.asInstanceOf[HTMLElement].blur()
    }
  }

  def decodeQueryParameters(query: String): Map[String, String | Seq[String]] = {
    var res: Map[String, String | Seq[String]] = Map()
    val decodedQuery = js.URIUtils.decodeURI(query)
    if (decodedQuery.isBlank() || !decodedQuery.startsWith("?")) return res
    decodedQuery
      .substring(1)
      .nn
      .split("&")
      .nn
      .map(_.nn)
      .foreach(param => {
        if (param.contains("=")) {
          val kv = param.split("=").nn
          val value = if (kv.length >= 2) kv(1).nn else "".nn
          if (kv(0).nn.matches(".*\\[\\]$")) {
            val key = kv(0).nn.replace("[]", "").nn
            if (!res.contains(key)) res += (key -> Seq(value))
            else {
              val oldVal = res.get(key).getOrElse(Seq())
              oldVal match {
                case oldVal: Seq[String] => res += (key -> (oldVal :+ value))
                case _                   => {}
              }

            }
          } else {
            res += (kv(0).nn -> (value))
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

    "[\\?,&][^\\?,&]*=$|[\\?,&]$|[\\?,&][^\\?,&]*=(?=[\\?,&])".r.replaceAllIn(res, "")
  }

  def countQueryParameters(validParams: Seq[String] = Seq.empty): Signal[Int] = Signal {
    queryParameters.value.count((p, _) => validParams.isEmpty || validParams.contains(p))
  }

  def getQueryParameterAsString(key: String): Signal[String] = query.map(query =>
    query.get(key).getOrElse("") match {
      case v: String      => v
      case v: Seq[String] => v.mkString
    },
  )

  def getQueryParameterAsSeq(key: String): Signal[Seq[String]] = query.map(query =>
    query.get(key).getOrElse(Seq()) match {
      case v: String      => Seq(v)
      case v: Seq[String] => v
    },
  )

  def cleanQueryParameters(newParams: Map[String, String | Seq[String]]) = {
    newParams.filter((key, value) =>
      value match {
        case x: String      => !x.isBlank
        case x: Seq[String] => x.size > 0 && x.filter(p => !p.isBlank).size > 0
      },
    )
  }

  def setQueryParameters(newParams: Map[String, String | Seq[String]]) = {
    query.set(cleanQueryParameters(newParams))
  }

  def updateQueryParameters(newParams: Map[String, String | Seq[String]]) = {
    query.transform(a => cleanQueryParameters(a ++ newParams))
  }

  def link(newPage: Page) =
    URL(linkPath(newPage), window.location.href).toString

  def linkPath(newPage: Page, newQuery: Map[String, String | Seq[String]] = Map()) = {
    Routes.toPath(newPage).pathString + encodeQueryParameters(newQuery)
  }

  def back() =
    window.history.back()

  query.map(query => {
    window.history.replaceState(null.asInstanceOf[js.Any], "", linkPath(page.now, query))
  }): @nowarn

  window.onpopstate = _ => {
    page.set(Routes.fromPath(Path(window.location.pathname)))
    query.set(decodeQueryParameters(window.location.search))
  }

  query.observe(t => page.map(page => linkPath(page, t))): @nowarn

}
