package de.sistar.experiments

import org.apache.camel.scala.dsl.builder.RouteBuilder
import akka.camel.{Consumer, CamelMessage,C}

class FilterRoute {

  class MyEndpoint extends Consumer {
    def endpointUri = "direct:x"

    def receive = {
      case CamelMessage(body, headers) => println("message = %s" format body)
      case _ => {}
    }
  }

  val service = CamelServiceManager.startCamelService
  service.awaitEndpointActivation(1) {
    actorOf[MyEndpoint].start
  }

  def createMyFilterRoute = new RouteBuilder {

    from("direct:start")
      .to("direct:x")
      .filter(_.in("gold") == "true")
      .to("mock:gold")
  }

}
