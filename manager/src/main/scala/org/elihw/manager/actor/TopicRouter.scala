package org.elihw.manager.actor

import akka.actor.{ActorRef, Props, Actor}
import org.elihw.manager.mail.{CreateMail, FreshTopicsMail}

/**
 * User: bigbully
 * Date: 13-11-23
 * Time: 下午1:30
 */
class TopicRouter extends Actor {

  import context._

  def receive: Actor.Receive = {
    case freshTopicsMail: FreshTopicsMail => {
      var topicMap: Map[String, ActorRef] = Map()
      for (topicName <- freshTopicsMail.topicList) yield {
        val topic = actorOf(Props[Topic], topicName)
        topic ! CreateMail(freshTopicsMail.brokerId, freshTopicsMail.broker)
        topicMap += (topicName -> topic)
      }
      sender ! topicMap
    }
  }
}
