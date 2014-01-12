package de.sistar.experiments.aggregate

import org.apache.camel._
import akka.actor._
import akka.camel.{Oneway, Producer}

case class MessageWithCounter(message: Message, recvMsgCount: Long) {
  def corid = message.getHeader("corid").asInstanceOf[Long]
}


object DeltaAggregator {
  val system = ActorSystem("DeltaAggregatorSystem")
  val retriever = system.actorOf(Props[MessageRetriever], name = "delta-retriever")
  val aggregator = system.actorOf(Props[DeltaAggregatorActor], name = "delta-aggregator")

}

class DeltaAggregator(){}


class DeltaAggregatorActor extends Actor {
  def endpointUri = "vm:aggregatorInput"

  private val toAggregated: ActorRef = context.actorOf(Props[SendToExternalCamelActor])

  def receive: Actor.Receive = {
    case l: List[MessageWithCounter] =>
      toAggregated ! l
    case msg: Message =>
      DeltaAggregator.aggregator ! msg
  }
}


class SendToExternalCamelActor extends Actor with Producer with Oneway {
  def endpointUri = "vm:aggregated"
}

