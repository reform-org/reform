package webapp

import webapp.services._

trait Services:
  lazy val routing: RoutingService

object ServicesDefault extends Services:
  lazy val routing = RoutingService()
