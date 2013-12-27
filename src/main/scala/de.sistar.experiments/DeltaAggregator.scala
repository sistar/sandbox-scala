package de.sistar.experiments

import org.apache.camel._
import akka.actor._

case class RequestMessage(message: Message, recvMsgCount: Long)


object DeltaAggregator {

  def apply(context: CamelContext): DeltaAggregator = {
    val pt = context.createProducerTemplate()
    new DeltaAggregator(system.actorOf(Props(classOf[DeltaAggregatorActor], pt.sendBody("direct:aggregated", _: Any))))
  }

  val system = ActorSystem("MySystem")
  val greeter = system.actorOf(Props[RequestMessageRetriever], name = "delta-aggregator")

}

class DeltaAggregatorActor(ptAgg: (Any) => Unit) extends Actor {
  def receive: Actor.Receive = {
    case l: List[RequestMessage] =>
      ptAgg(l)
    case msg: Message =>
      DeltaAggregator.greeter ! msg
  }
}

class DeltaAggregator(actor: ActorRef) extends Processor {
  def process(exchange: Exchange): Unit = {
    actor ! exchange.getIn
  }
}

