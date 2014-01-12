package de.sistar.experiments.aggregate

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.actor._
import akka.camel._
import scala.util.Try


case class MessageWithCounter(message: akka.camel.CamelMessage, recvMsgCount: Long) {
  def corid: Long = message.getHeaders.get("corid").asInstanceOf[Long]
}


object DeltaAggregator {
  def initActors(): ActorSystem = {
    val system = ActorSystem("DeltaAggregatorSystem")
    val camel = CamelExtension(system)

    val aggregator = system.actorOf(Props[DeltaAggregatorActor], name = "delta-aggregator")
    val activationFuture = camel.activationFutureFor(aggregator)(timeout = 10 seconds,
      executor = system.dispatcher)
    Await.result(activationFuture, 10 seconds)
    system
  }
}

class DeltaAggregatorActor extends Consumer {
  def endpointUri = "vm:aggregatorInput"

  private val retriever = context.actorOf(Props(classOf[MessageRetriever], 15L))
  private val toAggregated: ActorRef = context.actorOf(Props[SendToExternalCamelActor])

  def receive: Actor.Receive = {
    case l: List[MessageWithCounter] =>
      toAggregated ! l
    case msg: CamelMessage =>
      retriever ! msg
    case _ =>
      println("what?")
  }
}

class SendToExternalCamelActor extends Actor with Producer with Oneway {
  def endpointUri = "vm:aggregated"
}

