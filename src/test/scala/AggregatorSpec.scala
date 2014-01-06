import de.sistar.experiments.{JAggregatorRoute, AggregatorRoute}
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.apache.camel.testng.{CamelSpringTestSupport, CamelTestSupport}
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.testng.annotations.Test

class AggregatorSpec extends CamelSpringTestSupport with RouteBuilderSupport {

  override def createRouteBuilders() = {
    val xs = new AggregatorRoute().createMyFilterRoute
    val xs1: JAggregatorRoute = new JAggregatorRoute
    xs1.setContext(context)
    Array(xs,xs1)
  }
  override def createApplicationContext() = {new ClassPathXmlApplicationContext("test-context.xml")}

  @Test
  def shouldAggregateByTimeout() {

    getMockEndpoint("mock:a").expectedMessageCount(1)
    template.sendBodyAndHeader("direct:a", "Hello World", "corid", 1)
    assertMockEndpointsSatisfied()
  }

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