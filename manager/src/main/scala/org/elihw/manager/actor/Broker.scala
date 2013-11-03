package org.elihw.manager.actor

import akka.actor.Actor

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午7:50
 */
class Broker extends Actor{
  def receive = {
    case str:String => println(str)
  }
}
