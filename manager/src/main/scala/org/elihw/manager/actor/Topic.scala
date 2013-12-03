package org.elihw.manager.actor

import akka.actor.{ActorRef, Actor}
import org.elihw.manager.mail.{Mail, BrokerOfTopicResMail, BrokerOfTopicReqMail, CreateMail}


/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:02
 */
class Topic extends Actor {

  import context._

  var brokerMap: Map[String, ActorRef] = Map()
  var clientMap: Map[String, ActorRef] = Map()

  def receive = {
    case createMail: CreateMail => {
      createMail.from match {
        case Mail.BROKER => {
          brokerMap += (createMail.topicName -> actorSelection())
          broker ! FinishMail(self.path.name, self)
        }
      }

        //        case broker:Broker => {
        //          brokerMap += (createMail.id -> broker)
        //          broker ! FinishMail(self.path.name, self)
        //        }
        //        case client:Client => {
        //          clientMap += (createMail.id -> client)
        //          client ! FinishMail(self.path.name, self)
        //        }

    }
    case brokerOfTopicReqMail: BrokerOfTopicReqMail => {
      sender ! BrokerOfTopicResMail(brokerMap)
    }
  }
}
