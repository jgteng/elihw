package org.elihw.manager.actor

import akka.actor.{Status, ActorRef, Actor}
import org.elihw.manager.mail.{BrokerOfTopicResMail, BrokerOfTopicReqMail, FinishMail, CreateMail}

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:02
 */
class Topic extends Actor {

  var brokerMap: Map[String, ActorRef] = Map()
  var clientMap: Map[String, ActorRef] = Map()

  def receive = {
    case createMail: CreateMail => {
      sender match {
        case broker:Broker => {
          brokerMap += (createMail.id -> broker)
          broker ! FinishMail(self.path.name, self)
        }
        case client:Client => {
          clientMap += (createMail.id -> client)
          client ! FinishMail(self.path.name, self)
        }
      }
    }
    case brokerOfTopicReqMail:BrokerOfTopicReqMail => {
      sender ! BrokerOfTopicResMail(brokerMap)
    }
  }
}
