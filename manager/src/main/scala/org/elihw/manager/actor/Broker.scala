package org.elihw.manager.actor

import akka.actor.{ActorRef, Actor}
import org.elihw.manager.mail.{FinishMail, FreshTopicsMail, RegisterMail}
import org.elihw.manager.communication.BrokerServerHandler
import scala.collection.JavaConversions.asScalaBuffer
import akka.actor.Status.Success

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午7:50
 */
class Broker extends Actor {

  var topicMap: Map[String, ActorRef] = Map()
  var handler: BrokerServerHandler = null

  def receive = {
    case registerMail: RegisterMail => {
      handler = registerMail.handler
      val topicRouter = context.actorSelection("/manager/topicRouter")
      //转换java.util.list为inmutable.list
      var topicList = List[String]()
      for (topicName <- registerMail.cmd.getTopics) {
        topicList = topicList :+ topicName
      }
      //根据broker自带的topic刷新所有topic
      topicRouter ! FreshTopicsMail(topicList, registerMail.cmd.getId, self)
    }
    case finishMail: FinishMail => {
      topicMap += (finishMail.topicName -> finishMail.topic)
    }
    case Success => {
      handler finishRegister
    }
  }
}
