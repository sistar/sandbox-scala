package de.sistar.experiments.filter

import akka.camel._
import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._

object ActorFilterEndpoint {
  val system = ActorSystem("some-system")
  val camel = CamelExtension(system)
  val actorRef = system.actorOf(Props[ActorFilterEndpoint])


  // get a future reference to the activation of the endpoint of the Consumer Actor
  def activationFuture = camel.activationFutureFor(actorRef)(timeout = 10 seconds,
    executor = system.dispatcher)
}

class ActorFilterEndpoint extends Consumer {
  def endpointUri = "vm:x"

  val senderRef = context.actorOf(Props[SendToExternalCamelActor])

  def receive = {
    case CamelMessage(body, headers) =>
      println("message = %s with headers = %s" format( body, headers))
      if (headers.get("gold").getOrElse("false").asInstanceOf[String].toBoolean)
        senderRef ! "x"
    case _ =>
  }
}

import akka.actor.Actor
import akka.camel.{Producer, Oneway}

class SendToExternalCamelActor extends Actor with Producer with Oneway {
  def endpointUri = "vm:y"
}


