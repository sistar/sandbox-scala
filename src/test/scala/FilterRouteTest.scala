
import akka.actor.ActorRef
import de.sistar.experiments.filter.MyEndpoint
import org.apache.camel.testng.CamelSpringTestSupport
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.springframework.context.support.{ClassPathXmlApplicationContext, AbstractApplicationContext}
import org.testng.annotations.Test
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class FilterRouteTest extends CamelSpringTestSupport with RouteBuilderSupport {

  private val activationFuture: Future[ActorRef] = MyEndpoint.activationFuture

  def createApplicationContext(): AbstractApplicationContext = {
    Await.ready(activationFuture, 10 seconds)
    new ClassPathXmlApplicationContext("test-context.xml")

  }

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