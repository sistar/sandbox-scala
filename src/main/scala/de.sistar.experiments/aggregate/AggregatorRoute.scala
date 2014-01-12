package de.sistar.experiments.aggregate

import org.apache.camel.scala.dsl.builder.RouteBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy
import org.apache.camel.Exchange
import org.apache.camel.scala.SimplePeriod


object AggregatorRoute {
  val LOGGER: Logger = LoggerFactory.getLogger(classOf[AggregatorRoute])

}

class AggregatorRoute {

  new DeltaAggregator

  class MyListAS extends AbstractListAggregationStrategy[Integer] {
    def getValue(exchange: Exchange): Integer = exchange.getIn.getHeader("corid").asInstanceOf[Integer]
  }


  def createMyFilterRoute = new RouteBuilder {
    // timeout based
    "direct:a" ==> {
      aggregate(_.getIn().getHeader("corid"), new MyListAS()).completionTimeout(new SimplePeriod(6000)) {
        to("mock:a")
      }
    }

    // distance based Akka Actor
    "direct:b" to "vm:aggregatorInput"
    "vm:aggregated" to ("mock:b")
  }

}
