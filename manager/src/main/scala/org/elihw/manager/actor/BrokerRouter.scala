package org.elihw.manager.actor

import akka.actor.{Props, Actor}
import org.elihw.manager.mail.RegisterMail
import akka.pattern.ask
import akka.actor.Status.Status
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 下午5:21
 */
class BrokerRouter extends Actor{

  implicit val timeout = Timeout(1 seconds)

  def receive: Actor.Receive = {
    case registerMail:RegisterMail => {
      val broker = context.actorOf(Props[Broker], registerMail.cmd.getId.toString)
      val future = broker ? registerMail
      sender ! future.mapTo[Status]
    }
  }
}
