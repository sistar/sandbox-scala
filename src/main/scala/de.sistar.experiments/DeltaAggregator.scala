package de.sistar.experiments

import org.apache.camel._
import akka.actor._

case class RequestMessage(message: Message, recvMsgCount: Long)


object DeltaAggregator {

  def apply(context: CamelContext): DeltaAggregator = {

    new DeltaAggregator(system.actorOf(Props(classOf[DeltaAggregatorActor], context)))
  }

  val system = ActorSystem("MySystem")
  val greeter = system.actorOf(Props[RequestMessageRetriever], name = "delta-aggregator")

}

class DeltaAggregatorActor(camelContext: CamelContext) extends Actor {
  val pt = camelContext.createProducerTemplate()
  def receive: Actor.Receive = {
    case l: List[RequestMessage] =>
      pt.sendBody("direct:aggregated",l)
    case msg: Message =>
      DeltaAggregator.greeter ! msg
  }
}

class DeltaAggregator(actor: ActorRef) extends Processor {
  def process(exchange: Exchange): Unit = {
    actor ! exchange.getIn
  }
}

