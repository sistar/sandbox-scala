package de.sistar.experiments

import akka.actor._
import akka.contrib.pattern.Aggregator
import scala.collection._

case object CantUnderstand

class GreetingAggregator(originalsender: ActorRef, corid: Long) extends Actor with Aggregator with ActorLogging {
  val results =
    mutable.ArrayBuffer.empty[RequestMessage]
  expect {
    case greeting: RequestMessage =>
      results += greeting
      log.info("agg msg: " + greeting.message)
    case actCount: Long =>
      if (results.nonEmpty)
        if (actCount - results.max(Ordering.by((r: RequestMessage) => r.recvMsgCount)).recvMsgCount > 3)
          originalsender ! results.toList
  }
}

class RequestMessageRetriever extends Actor with Aggregator {

  var counter: Long = 0
  val map = scala.collection.mutable.HashMap.empty[Long, ActorRef]
  expect {
    case message: org.apache.camel.Message =>
      counter += 1
      val g = new RequestMessage(message, counter)
      val corid = g.message.getHeader("corid").asInstanceOf[Long]
      val x = map getOrElseUpdate(corid, context.actorOf(Props(classOf[GreetingAggregator], sender, corid)))
      x ! g
      map.values.foreach(_ ! counter)
    case _ =>
      sender ! CantUnderstand
      context.stop(self)
  }


}
