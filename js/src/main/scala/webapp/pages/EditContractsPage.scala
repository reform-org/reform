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
package webapp.pages

import outwatch.*
import outwatch.dsl.*
import rescala.default
import rescala.default.*
import webapp.components.navigationHeader
import webapp.services.Page
import webapp.entity.*
import webapp.{*, given}
import webapp.services.DiscoveryService
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.services.{ToastMode, Toaster}
import webapp.repo.Synced
import webapp.components.common.*
import webapp.utils.Futures.*
import webapp.services.ToastType
import scala.scalajs.js.Date

case class EditContractsPage(contractId: String)(using
    repositories: Repositories,
    toaster: Toaster,
    routing: RoutingService,
) extends Page {

  private val existingValue = repositories.contracts.all.map(_.find(c => c.id == contractId))

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode = {
    div(
      existingValue
        .map(currentContract => {
          val result: VMod = currentContract match {
            case Some(currentContract) =>
              InnerEditContractsPage(Some(currentContract)).render()
            case None =>
              navigationHeader(
                div(
                  div(
                    cls := "p-1",
                    h1(cls := "text-4xl text-center", "EditContractsPage"),
                  ),
                  h2("Contract not found"),
                ),
              )
          }
          result
        }),
    )
  }
}

case class InnerEditContractsPage(existingValue: Option[Synced[Contract]])(using
    toaster: Toaster,
    repositories: Repositories,
    routing: RoutingService,
) {
  val startEditEntity = existingValue.map(_.signal.now)

  private def contractAssociatedProject(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.projects.all.map(_.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse("")))),
      )
      .withLabel("Project")
      .require
      .bindAsSelect(
        _.contractAssociatedProject,
        (p, a) => p.copy(contractAssociatedProject = a),
      )
  }

  private def contractAssociatedHiwi(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.hiwis.all.map(list =>
          list.map(value =>
            value.id -> value.signal.map(v => v.firstName.get.getOrElse("") + " " + v.lastName.get.getOrElse("")),
          ),
        ),
      )
      .withLabel("Associated Hiwi")
      .require
      .bindAsSelect(
        _.contractAssociatedHiwi,
        (p, a) => p.copy(contractAssociatedHiwi = a),
      )
  }

  private def contractAssociatedSupervisor(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        options = repositories.supervisors.all.map(list =>
          list.map(value =>
            value.id -> value.signal.map(v => v.firstName.get.getOrElse("") + " " + v.lastName.get.getOrElse("")),
          ),
        ),
      )
      .withLabel("Associated Supervisors")
      .require
      .bindAsSelect(
        _.contractAssociatedSupervisor,
        (p, a) => p.copy(contractAssociatedSupervisor = a),
      )
  }

  private def contractAssociatedType(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.contractSchemas.all.map(list =>
          list.map(value => value.id -> value.signal.map(v => v.name.get.getOrElse(""))),
        ),
      )
      .withLabel("Contract Type")
      .require
      .bindAsSelect(
        _.contractType,
        (p, a) => p.copy(contractType = a),
      )
  }

  private val contractStartDate = UIAttributeBuilder.date
    .withLabel("Start Date")
    .require
    .bindAsDatePicker[Contract](
      _.contractStartDate,
      (h, a) => h.copy(contractStartDate = a),
    )

  private val contractEndDate = UIAttributeBuilder.date
    .withLabel("Start Date")
    .require
    .bindAsDatePicker[Contract](
      _.contractEndDate,
      (h, a) => h.copy(contractEndDate = a),
    )

  private val contractHoursPerMonth = UIAttributeBuilder.int
    .withLabel("Hours per Month")
    .require
    .bindAsNumber[Contract](
      _.contractHoursPerMonth,
      (h, a) => h.copy(contractHoursPerMonth = a),
    )

  private def contractAssociatedPaymentLevel(using repositories: Repositories): UIAttribute[Contract, String] = {
    UIAttributeBuilder
      .select(
        repositories.paymentLevels.all.map(list =>
          list.map(value => value.id -> value.signal.map(v => v.title.get.getOrElse(""))),
        ),
      )
      .withLabel("Associated PaymentLevel")
      .require
      .bindAsSelect(
        _.contractAssociatedPaymentLevel,
        (p, a) => p.copy(contractType = a),
      )
  }

  private def createOrUpdate(): Unit = {
    val editingNow = editingValue.now.get._2.now
    existingValue match {
      case Some(existing) => {
        existing
          .update(p => {
            p.getOrElse(Contract.empty).merge(editingNow)
          })
          .map(value => {
            editingValue.set(None)
            toaster.make(
              "Contract saved!",
              ToastMode.Short,
              ToastType.Success,
            )
          })
          .map(value => {
            routing.to(ContractsPage())
          })
          .toastOnError(ToastMode.Infinit)
      }
      case None => {
        repositories.contracts
          .create()
          .flatMap(entity => {
            editingValue.set(Some((Contract.empty.default, Var(Contract.empty.default))))
            //  TODO FIXME we probably should special case initialization and not use the event
            entity.update(p => {
              p.getOrElse(Contract.empty).merge(editingNow)
            })
          })
          .map(value => {
            routing.to(ContractsPage())
          })
          .toastOnError(ToastMode.Infinit)
      }
    }
  }

  private def cancelEdit(): Unit = {
    routing.to(ContractsPage())
  }

  var editingValue: Var[Option[(Contract, Var[Contract])]] = Var(
    Option(existingValue.get.signal.now, Var(existingValue.get.signal.now)),
  )

  def render(using
      routing: RoutingService,
      repositories: Repositories,
      webrtc: WebRTCService,
      discovery: DiscoveryService,
      toaster: Toaster,
  ): VNode =
    navigationHeader(
      div(
        div(
          cls := "p-1",
          h1(cls := "text-4xl text-center", "EditContractsPage"),
        ),
        div(
          p("Editing Contract:"),
          label(existingValue.map(p => p.id)),
          form(
            p("Basic information"),
            div(
              div(
                label("Hiwi:"),
                contractAssociatedHiwi.renderEdit("", editingValue),
                br,
                label("Supervisor:"),
                contractAssociatedSupervisor.renderEdit("", editingValue),
              ),
              div(
                label("Start date:"),
                contractStartDate.renderEdit("", editingValue),
                p(
                  editingValue.map(p =>
                    p.get._2.map(v => {
                      if (v.contractEndDate.get.getOrElse(0L) - v.contractStartDate.get.getOrElse(0L) > 0)
                        "Duration in days: " + (v.contractEndDate.get.getOrElse(0L) - v.contractStartDate.get
                          .getOrElse(0L)).toString()
                      else ""
                    }),
                  ),
                  editingValue.map(p =>
                    p.get._2.map(v => {
                      if (
                        v.contractStartDate.get.getOrElse(0L) - (Date
                          .now() / (1000 * 3600 * 24)).toLong < 0 && v.contractStartDate.get.getOrElse(0L) != 0
                      ) "Start date is in the past"
                      else ""

                    }),
                  ),
                ),
                label("End date:"),
                contractEndDate.renderEdit("", editingValue),
                p(
                  editingValue.map(p =>
                    p.get._2.map(v => {
                      if (
                        v.contractEndDate.get.getOrElse(0L) - v.contractStartDate.get
                          .getOrElse(0L) < 0 && v.contractEndDate.get.getOrElse(0L) != 0
                      ) "End date is in the past or before start date"
                      else ""

                    }),
                  ),
                ),
                // Todo Warning
              ),
              div(
                label("Hours per month:"),
                contractHoursPerMonth.renderEdit("", editingValue),
                label("Payment level:"),
                contractAssociatedPaymentLevel.renderEdit("", editingValue),
                // TODO calculation of monthly base salary and total hours
                p("Monthly base salary: 1.500€; with bonus: 1.800€"),
                p("Total Hours: 160h"),
              ),
            ),
            // Select Project Field
            p("Select project"),
            div(
              label("Project:"),
              contractAssociatedProject.renderEdit("", editingValue),
            ),
            // Contract Type Field
            p("ContractType:"),
            div(
              // TODO active contract checking
              p("Hiwi did not have an active contract ..."),
              label("ContractType:"),
              contractAssociatedType.renderEdit("", editingValue),
            ),
            button(
              cls := "btn",
              `type` := "submit",
              idAttr := "confirmEdit",
              "Save",
            ),
            TableButton(LightButtonStyle.Default, "Cancel", onClick.foreach(_ => cancelEdit())),
            onSubmit.foreach(e => {
              e.preventDefault()
              createOrUpdate()
            }),
          ),
        ),
      ),
    )

}
