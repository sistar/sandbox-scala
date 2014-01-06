
import de.sistar.experiments.FilterRoute
import org.apache.camel.test.junit4.CamelTestSupport
import org.junit.Test
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport


class FilterRouteTest extends CamelTestSupport with RouteBuilderSupport {

  // then override the createRouteBuilder method to provide the route we want to test
  override def createRouteBuilder() = new FilterRoute().createMyFilterRoute

  // and here we just have regular JUnit test method which uses the API from camel-test

  @Test
  def testFilterRouteGold() {
    getMockEndpoint("mock:gold").expectedMessageCount(1)

    template.sendBodyAndHeader("direct:start", "Hello World", "gold", "true")

    assertMockEndpointsSatisfied()
  }

  @Test
  def testFilterRouteNotGold() {
    getMockEndpoint("mock:gold").expectedMessageCount(0)

    template.sendBodyAndHeader("direct:start", "Hello World", "gold", "false")

    assertMockEndpointsSatisfied()
  }

}