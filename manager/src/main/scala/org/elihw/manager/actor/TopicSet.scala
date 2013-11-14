package org.elihw.manager.actor

import akka.actor.{ActorRef, Props, Actor}
import org.elihw.manager.mail.BrokerRegister

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:08
 */
class TopicSet extends Actor{

  var topicSet:Set[ActorRef] = Set()

  def receive: Actor.Receive = {
    case brokerRegister:BrokerRegister => {
      println(brokerRegister)
      for(topicName <- brokerRegister.topicNames){
        val topic = context.actorOf(Props[Topic], "./" + topicName)
        topicSet + topic
      }
    }
  }
}
