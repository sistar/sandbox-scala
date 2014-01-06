package de.sistar.experiments

import org.apache.camel.scala.dsl.builder.RouteBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.camel.component.mock.MockEndpoint
import scala.util.Random
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy
import org.apache.camel.{Predicate, Exchange}
import org.apache.camel.scala.SimplePeriod


object AggregatorRoute {
  val LOGGER: Logger = LoggerFactory.getLogger(classOf[AggregatorRoute])
  val random = new Random()

  def assertMockendp(items: MockEndpoint) =
    MockEndpoint.assertIsSatisfied(items)
}

class AggregatorRoute {

  def actSerial: Long = 1

  class MyListAS extends AbstractListAggregationStrategy[Integer] {
    def getValue(exchange: Exchange): Integer = exchange.getIn.getHeader("corid").asInstanceOf[Integer]
  }

  class MyP extends Predicate {
    def matches(exchange: Exchange): Boolean = actSerial - exchange.getIn().getHeader("serial").asInstanceOf[Long] > 500
  }

  def createMyFilterRoute = new RouteBuilder {
    "direct:a" ==> {
      aggregate(_.getIn().getHeader("corid"), new MyListAS()).completionTimeout(new SimplePeriod(6000)) {
        to("mock:a")
      }
    }

    "direct:aggregated" to ("mock:b")

    private val deltaAggregator = DeltaAggregator(getContext)
    "direct:b" process deltaAggregator
  }

}
