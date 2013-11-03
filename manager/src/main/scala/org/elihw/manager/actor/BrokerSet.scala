package org.elihw.manager.actor

import akka.actor.Actor

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午7:49
 */
class BrokerSet extends Actor{

  def receive: Actor.Receive = {
    case str => println(str)
  }
}
