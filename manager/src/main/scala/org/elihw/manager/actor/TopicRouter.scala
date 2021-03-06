package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail._
import akka.pattern._
import scala.concurrent.{Await, Future}
import akka.util.Timeout
import scala.concurrent.duration._
import org.elihw.manager.mail.CreateMail
import org.elihw.manager.mail.PublishTopicsMail
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import org.elihw.manager.actor.Topic.TopicInfo
import java.util.{Timer, TimerTask}


/**
 * User: bigbully
 * Date: 13-11-23
 * Time: 下午1:30
 */
class TopicRouter extends Actor with ActorLogging{

  import Mail._
  import context._

  implicit val timeout = Timeout(1 seconds)
  var busyTopic:Set[String] = Set()

  def receive: Actor.Receive = {
    case publishTopicsMail: PublishTopicsMail => {
      for (topicName <- publishTopicsMail.topicList) {
        val topic = actorSelection("/user/manager/topicRouter/" + topicName)
        val future: Future[ActorIdentity] = ask(topic, Identify(topicName)).mapTo[ActorIdentity]
        val creator = sender
        future.foreach {
          (actorIdentity: ActorIdentity) => {
            actorIdentity match {
              case ActorIdentity(topicName, Some(ref)) => {
                ref ! CreateMail(topicName.toString, creator.path.name, publishTopicsMail.from)
              }
              case ActorIdentity(topicName, None) => {
                try {
                  val topic = actorOf(Props[Topic], topicName.asInstanceOf[String])
                  topic ! CreateMail(topicName.toString, creator.path.name, publishTopicsMail.from)
                } catch {
                  case e: InvalidActorNameException => {
                    //高并发情况下会出现
                    val topicRef = actorSelection("/user/manager/topicRouter/" + topicName)
                    topicRef ! CreateMail(topicName.toString, creator.path.name, publishTopicsMail.from)
                  }
                }
              }
            }
          }
        }
      }
    }
    case StatusMail => {
      var list: List[TopicInfo] = List()
      children.foreach {
        child => {
          list +:= Await.result((child ? StatusMail), timeout.duration).asInstanceOf[TopicInfo]
        }
      }
      sender ! StatusResMail(Mail.TOPIC, list)
    }
    case startBusyTopicCheckMail:StartBusyTopicCheckMail => {
      new Timer().schedule(new BusyTopicCheckTask, startBusyTopicCheckMail.delay, startBusyTopicCheckMail.checkPeriod)
    }
    case topicInfosMail:TopicInfosMail => {
      for(topicInfo <- topicInfosMail.topicInfos) {
        child(topicInfo.getTopicName) match {
          case Some(ref) => {
            ref ! TopicHeartInfoMail(topicInfosMail.brokerId, topicInfo)
          }
          case None => {
            log.debug("topic:{}已销毁,不再接受心跳", topicInfo.getTopicName)
          }
        }
      }
    }
  }

  class BusyTopicCheckTask extends TimerTask {
    def run = {
      for(topic <- children) {
        topic ! AreYouBusyNow
      }
    }
  }

}
