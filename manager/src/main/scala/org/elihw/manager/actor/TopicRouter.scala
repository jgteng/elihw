package org.elihw.manager.actor

import akka.actor.{ActorRef, Props, Actor}
import org.elihw.manager.mail.{CreateMail, FreshTopicsMail}
import akka.actor.Status.Success

/**
 * User: bigbully
 * Date: 13-11-23
 * Time: 下午1:30
 */
class TopicRouter extends Actor {

  import context._

  def receive: Actor.Receive = {
    case freshTopicsMail: FreshTopicsMail => {
      for (topicName <- freshTopicsMail.topicList){
        val topic = actorOf(Props[Topic], topicName)
        topic ! CreateMail(freshTopicsMail.brokerId, freshTopicsMail.broker)
      }
      sender ! Success(self)
    }
  }
}
