package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail.CreateMail
import org.elihw.manager.mail.PublishTopicsMail
import akka.actor.Identify
import akka.actor.Status.Success

/**
 * User: bigbully
 * Date: 13-11-23
 * Time: 下午1:30
 */
class TopicRouter extends Actor {

  import context._

  def receive: Actor.Receive = {
    case freshTopicsMail: PublishTopicsMail => {
      for (topicName <- freshTopicsMail.topicList) {
        val topic = actorSelection("/user/manager/topicRouter/" + topicName)
        topic ! Identify(topicName)
      }
    }
    case ActorIdentity(topicName, Some(ref)) => {
      sender ! Success(self)
    }
    case ActorIdentity(topicName, None) => {
      val topic = actorOf(Props[Topic], topicName.asInstanceOf[String])
      topic ! CreateMail(sender.path.name)
      sender ! Success(self)
    }
  }
}
