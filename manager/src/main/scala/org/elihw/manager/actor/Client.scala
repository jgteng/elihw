package org.elihw.manager.actor

import akka.actor.{ActorRef, Actor}
import org.elihw.manager.mail._
import org.elihw.manager.communication.ClientServerHandler
import org.elihw.manager.mail.BrokerOfTopicReqMail
import org.elihw.manager.mail.PublishMail
import org.elihw.manager.mail.FinishMail

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:04
 */
class Client(val handler:ClientServerHandler) extends Actor {

  var topic: ActorRef = null
  var brokerMap: Map[String, ActorRef] = Map()

  def receive = {
    case publishMail: PublishMail => {
      val topicRouter = context.actorSelection("/user/manager/topicRouter")
      topicRouter ! PublishTopicsMail(List(publishMail.cmd.getTopicName), Mail.CLIENT)
    }
    case finishMail: FinishMail => {
      topic = finishMail.topic
      topic ! BrokerOfTopicReqMail(self.path.name)
    }
    case brokerOfTopicResMail: BrokerOfTopicResMail => {
      brokerMap = brokerOfTopicResMail.brokerMap
      handler finishRegister(self, brokerMap)
    }
  }
}
