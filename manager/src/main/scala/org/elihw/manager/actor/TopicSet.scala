package org.elihw.manager.actor

import akka.actor.Actor
import org.elihw.manager.mail.BrokerRegister

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:08
 */
class TopicSet extends Actor{

  def receive: Actor.Receive = {
    case brokerRegister:BrokerRegister => {

    }
  }
}
