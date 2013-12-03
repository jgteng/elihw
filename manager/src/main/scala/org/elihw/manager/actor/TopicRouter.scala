package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail.{PublishTopicsMail, CreateMail}
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
        val future: Future[ActorIdentity] = ask(topic, Identify(topicName)).mapTo[ActorIdentity]
        future.foreach {
          (actorIdentity: ActorIdentity) => {
            actorIdentity match {
              case ActorIdentity(topicName, Some(ref)) => {
                ref ! CreateMail(topicName.toString, sender.path.name, publishTopicsMail.from)
              }
              case ActorIdentity(topicName, None) => {
                val topic = actorOf(Props[Topic], topicName.asInstanceOf[String])
                topic ! CreateMail(topicName.toString, sender.path.name, publishTopicsMail.from)
              }
            }
          }
        }
      }
    }
  }
}
