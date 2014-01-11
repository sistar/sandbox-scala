package de.sistar.experiments.filter

import akka.camel._
import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._

object MyEndpoint {
  val system = ActorSystem("some-system")
  val camel = CamelExtension(system)
  val actorRef = system.actorOf(Props[MyEndpoint])


  // get a future reference to the activation of the endpoint of the Consumer Actor
  def activationFuture = camel.activationFutureFor(actorRef)(timeout = 10 seconds,
    executor = system.dispatcher)
}

class MyEndpoint extends Consumer {
  def endpointUri = "vm:x"
  val ordersRef = context.actorOf(Props[Orders])
  def receive = {
    case CamelMessage(body, headers) => {
      println("message = %s" format body)
      ordersRef ! "x"
    }
    case _ => {}
  }
}

import akka.actor.Actor
import akka.camel.{Producer, Oneway}

class Orders extends Actor with Producer with Oneway {
  def endpointUri = "vm:y"
}


