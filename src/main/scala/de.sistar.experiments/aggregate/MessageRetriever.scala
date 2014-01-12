package de.sistar.experiments.aggregate

import akka.actor._
import akka.contrib.pattern.Aggregator
import scala.collection._

case object CantUnderstand


class CoridDedicatedAggregator(originalSender: ActorRef,
                               corid: Long,
                               allowedDistance: Long) extends Actor with Aggregator with ActorLogging {
  val results =
    mutable.ArrayBuffer.empty[MessageWithCounter]
  expect {
    case m: MessageWithCounter =>
      results += m
      log.info("agg msg: " + m.message)
    case actGlobalCounter: Long =>
      if (results.nonEmpty) {
        if (actGlobalCounter - results.last.recvMsgCount > allowedDistance)
          originalSender ! results.toList
        sender ! this
      }
  }

  def getCorid = corid
}

/**
 * retrieve message, add counter and send it to an dedicated actor for its corid
 */
class MessageRetriever(allowedDistance: Long) extends Actor with Aggregator {

  var counter: Long = 0
  val coridToByCoridAggregatorMap = scala.collection.mutable.HashMap.empty[Long, ActorRef]
  expect {
    case message: org.apache.camel.Message =>
      counter += 1
      val messageWithCounter = new MessageWithCounter(message, counter)
      getOrCreateActorForCorid(messageWithCounter.corid) ! messageWithCounter
      coridToByCoridAggregatorMap.values.foreach(_ ! counter)
    case c: CoridDedicatedAggregator =>
      val actorToKill: Option[ActorRef] = coridToByCoridAggregatorMap.get(c.getCorid)
      actorToKill.map(_ ! akka.actor.PoisonPill)
      coridToByCoridAggregatorMap -= c.getCorid
    case _ =>
      sender ! CantUnderstand
      context.stop(self)
  }

  def getOrCreateActorForCorid(corid: Long): ActorRef = {
    coridToByCoridAggregatorMap getOrElseUpdate(corid, context.actorOf(
      Props(classOf[CoridDedicatedAggregator], sender, corid, allowedDistance)))
  }
}
