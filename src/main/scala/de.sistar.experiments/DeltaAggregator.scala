package de.sistar.experiments

import org.apache.camel._
import akka.actor.{Props, Actor, ActorLogging, ActorSystem}

case class Greeting(message: Message)

class GreetingActor extends Actor with ActorLogging {
  def receive = {
    case Greeting(msg) ⇒ log.info("Hello " + msg)
  }
}

class DeltaAggregator extends Processor {


  val system = ActorSystem("MySystem")
  val greeter = system.actorOf(Props[GreetingActor], name = "greeter")

  def process(exchange: Exchange): Unit = {
    greeter ! Greeting(exchange.getIn)

  }
}
