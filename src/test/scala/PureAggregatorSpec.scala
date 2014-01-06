import de.sistar.experiments.{JAggregatorRoute, AggregatorRoute}
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.apache.camel.testng.{CamelTestSupport, CamelSpringTestSupport}
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.testng.annotations.Test

class PureAggregatorSpec extends CamelTestSupport with RouteBuilderSupport {

  override def createRouteBuilder() =  new AggregatorRoute().createMyFilterRoute

  @Test
  def shouldAggregateByActor() {

    getMockEndpoint("mock:b").expectedMessageCount(1)
    template.sendBodyAndHeader("direct:b", "Hello World-1-1", "corid", 1L)
    template.sendBodyAndHeader("direct:b", "Hello World-2-1", "corid", 2L)
    template.sendBodyAndHeader("direct:b", "Hello World-2-2", "corid", 2L)
    template.sendBodyAndHeader("direct:b", "Hello World-2-3", "corid", 2L)
    template.sendBodyAndHeader("direct:b", "Hello World-2-4", "corid", 2L)
    template.sendBodyAndHeader("direct:b", "Hello World-2-5", "corid", 2L)
    assertMockEndpointsSatisfied()
  }

}