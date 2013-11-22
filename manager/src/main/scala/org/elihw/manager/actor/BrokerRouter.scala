package org.elihw.manager.actor

import akka.actor.{Props, Actor}
import org.elihw.manager.mail.BrokerRegisterMail

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 下午5:21
 */
class BrokerRouter extends Actor{

  def receive: Actor.Receive = {
    case registerMail:BrokerRegisterMail => {
      val handler = registerMail.brokerServerHandler
      context.actorOf(Props[Broker], "")
    }
  }
}
