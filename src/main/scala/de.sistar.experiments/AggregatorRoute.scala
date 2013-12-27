package de.sistar.experiments

import org.apache.camel.impl.{DefaultProducerTemplate, DefaultCamelContext}
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

  def main(args: Array[String]): Unit = {

    /** val maxReads: Long = args(0).toLong
    val topic: String = args(1)
    val partition: Int = args(2).toInt **/


    try {
      val camelContext = new DefaultCamelContext()
      camelContext.addRoutes(new AggregatorRoute)
      camelContext.start()
      val mockEndpoint: MockEndpoint = camelContext.getEndpoint("mock:a").asInstanceOf[MockEndpoint]
      mockEndpoint.expectedMinimumMessageCount(10000)

      val instance: DefaultProducerTemplate = DefaultProducerTemplate.newInstance(camelContext, "direct:a")
      instance.start()
      for (i <- 1 to 1000000)
        instance.sendBodyAndHeader("Hello!", "corid", random.nextInt(10000))
      assertMockendp(mockEndpoint)
      Thread.sleep(300)
      camelContext.stop()
    }
    catch {
      case e: Exception => {

        e.printStackTrace()
      }
    }
  }
}

class AggregatorRoute extends RouteBuilder {
  def actSerial: Long = 1

  class MyListAS extends AbstractListAggregationStrategy[Integer] {
    def getValue(exchange: Exchange): Integer = exchange.getIn.getHeader("corid").asInstanceOf[Integer]
  }

  class MyP extends Predicate {
    def matches(exchange: Exchange): Boolean = actSerial - exchange.getIn().getHeader("serial").asInstanceOf[Long] > 500
  }


  "direct:a" ==> {
    aggregate(_.getIn().getHeader("corid"), new MyListAS()).completionTimeout(new SimplePeriod(6000)) {
      to("mock:a")
    }
  }





  "direct:aggregated" to ("mock:b")
  private val deltaAggregator = DeltaAggregator(getContext)
  "direct:b" process deltaAggregator
}
