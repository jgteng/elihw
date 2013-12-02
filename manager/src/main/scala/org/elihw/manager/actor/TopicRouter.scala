package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail.{PublishTopicsMail, PublishTopicMail, CreateMail}
import akka.actor.Identify
import akka.pattern._
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * User: bigbully
 * Date: 13-11-23
 * Time: 下午1:30
 */
class TopicRouter extends Actor {

  import context._

  implicit val timeout = Timeout(1 seconds)

  def receive: Actor.Receive = {
    case publishTopicsMail: PublishTopicsMail => {
      for (topicName <- publishTopicsMail.topicList) {
        val topic = actorSelection("/user/manager/topicRouter/" + topicName)
        val creator = sender
        val future: Future[ActorIdentity] = ask(topic, Identify(topicName)).mapTo[ActorIdentity]
        future.foreach {
          (actorIdentity: ActorIdentity) => {
            actorIdentity match {
              case ActorIdentity(topicName, Some(ref)) => {
                ref ! CreateMail(sender.path.name, creator)
              }
              case ActorIdentity(topicName, None) => {
                val topic = actorOf(Props[Topic], topicName.asInstanceOf[String])
                topic ! CreateMail(sender.path.name, creator)
              }
            }
          }
        }
      }
    }
    case publishTopicMail: PublishTopicMail => {
      val topic = actorSelection("/user/manager/topicRouter/" + publishTopicMail.topicName)
      topic ! Identify(publishTopicMail.topicName)
    }
  }
}
