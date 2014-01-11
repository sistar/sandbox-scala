
import akka.actor.ActorRef
import de.sistar.experiments.filter.ActorFilterEndpoint
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.testng.CamelSpringTestSupport
import org.apache.camel.scala.dsl.builder.RouteBuilderSupport
import org.springframework.context.support.{ClassPathXmlApplicationContext, AbstractApplicationContext}
import org.testng.annotations.Test
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class FilterRouteTest extends CamelSpringTestSupport with RouteBuilderSupport {

  private val activationFuture: Future[ActorRef] = ActorFilterEndpoint.activationFuture

  def createApplicationContext(): AbstractApplicationContext = {
    Await.ready(activationFuture, 10 seconds)
    new ClassPathXmlApplicationContext("test-context.xml")
  }

  @Test
  def testFilterRouteGold() {
    goldShouldReceiveNumberOfMessages(1)

    template.sendBodyAndHeader("direct:start", "Hello World", "gold", "true")

    assertMockEndpointsSatisfied()
  }
  
  @Test
  def testMessageWithoutFilterAttributeShouldBeFiltered() {
    goldShouldReceiveNumberOfMessages(0)

    template.sendBodyAndHeader("direct:start", "Hello World", "foo", "true")

    assertMockEndpointsSatisfied()
  }

  @Test
  def testFilterRouteNotGold() {
    goldShouldReceiveNumberOfMessages(0)

    template.sendBodyAndHeader("direct:start", "Hello World", "gold", "false")

    assertMockEndpointsSatisfied()
  }

  def goldShouldReceiveNumberOfMessages(numberOfMessages: Int = 1) {
    val mockEndpoint: MockEndpoint = getMockEndpoint("mock:gold")
    mockEndpoint.expectedMessageCount(numberOfMessages)
    mockEndpoint.setAssertPeriod(1000L)
  }
}
