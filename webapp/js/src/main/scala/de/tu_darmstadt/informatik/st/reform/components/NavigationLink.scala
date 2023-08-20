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
package de.tu_darmstadt.informatik.st.reform.components

import de.tu_darmstadt.informatik.st.reform.*
import de.tu_darmstadt.informatik.st.reform.services.Page
import org.scalajs.dom
import org.scalajs.dom.HTMLElement
import outwatch.*
import outwatch.dsl.*

def navigationLink(using jsImplicits: JSImplicits)(page: Page, label: String): VNode = {
  a(
    cls := "btn btn-ghost normal-case	font-normal rounded-md	hover:bg-slate-100 dark:hover:bg-gray-800/50",
    label,
    onClick.foreach(e => {
      e.preventDefault()
      e.target.asInstanceOf[HTMLElement].blur()
      if (e.ctrlKey) {
        jsImplicits.routing.to(page, true)
      } else {
        jsImplicits.routing.to(page, false)
      }
    }),
    href := jsImplicits.routing.linkPath(page),
  )
}

def navigationIconLink(using jsImplicits: JSImplicits)(page: Page, icon: VNode): VNode = {
  a(
    cls := "btn btn-ghost normal-case	font-normal rounded-md	hover:bg-slate-100 dark:hover:bg-gray-800/50",
    icon,
    onClick.foreach(e => {
      e.preventDefault()
      e.target.asInstanceOf[HTMLElement].blur()
      if (e.ctrlKey) {
        jsImplicits.routing.to(page, true)
      } else {
        jsImplicits.routing.to(page, false)
      }
    }),
    href := jsImplicits.routing.linkPath(page),
  )
}
