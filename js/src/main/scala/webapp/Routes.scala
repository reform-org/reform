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
import webapp.pages.*
import webapp.services.Page

object Routes {
  def fromPath(using repositories: Repositories): Path => Page = {
    case Root                      => HomePage()
    case Root / "projects"         => ProjectsPage()
    case Root / "users"            => UsersPage()
    case Root / "hiwis"            => HiwisPage()
    case Root / "paymentlevels"    => PaymentLevelsPage()
    case Root / "salarychanges"    => SalaryChangesPage()
    case Root / "supervisor"       => SupervisorsPage()
    case Root / "contractSchema"   => ContractSchemasPage()
    case Root / "requiredDocument" => RequiredDocumentsPage()
  }

  def toPath: Page => Path = {
    case HomePage()              => Root / ""
    case ProjectsPage()          => Root / "projects"
    case UsersPage()             => Root / "users"
    case HiwisPage()             => Root / "hiwis"
    case PaymentLevelsPage()     => Root / "paymentlevels"
    case SalaryChangesPage()     => Root / "salarychanges"
    case SupervisorsPage()       => Root / "supervisor"
    case ContractSchemasPage()   => Root / "contractSchema"
    case RequiredDocumentsPage() => Root / "requiredDocument"
  }
}
