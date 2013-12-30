package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail._
import akka.pattern._
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import org.elihw.manager.mail.CreateMail
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import org.elihw.manager.other.{BusyTopicLevel, Info}
import org.elihw.manager.actor.Topic.TopicInfo
import com.jd.bdp.whale.common.command.TopicHeartInfo

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:02
 */
class Topic extends Actor with ActorLogging {

  import context._
  import Mail._

  implicit val timeout = Timeout(1 seconds)

  val brokerRouter = actorSelection("/user/manager/brokerRouter")
  var brokers: Set[ActorPath] = Set()
  var clients: Set[ActorPath] = Set()
  var broker2topicInfo: Map[String, TopicHeartInfo] = Map()

  def confirmAndUpdateStatus(id: String, from: String) = {
    val future: Future[ActorIdentity] = ask(actorSelection("/user/manager/" + from + "Router/" + id), Identify(id)).mapTo[ActorIdentity]
    future.foreach {
      (actorIdentity: ActorIdentity) => {
        actorIdentity match {
          case ActorIdentity(id, Some(ref)) => {
            from match {
              case Mail.BROKER => {
                log.debug("topic:{}关联broker:{}", self.path.name, id)
                brokers += ref.path
                //通知所有client，又有broker上线了
                for(clientPath <- clients){
                  actorSelection(clientPath) ! BrokerOnLine(ref.path.name)
                }
                ref ! FinishMail(self.path)
              }
              case Mail.CLIENT => {
                log.debug("topic:{}关联client:{}", self.path.name, id)
                clients += ref.path
                if (brokers.isEmpty) {
                   brokerRouter ! FindLazyBrokersMail(self.path, ref.path) //把client也一并传过去，查找完lazyBroker之后再创建
                } else {
                  ref ! FinishMail(self.path, brokers)
                }
              }
            }
          }
          case ActorIdentity(id, None) => {
            log.info("当前{}{}已经被销毁,忽略", from, id)
          }
        }
      }
    }
  }

  def iAmBusy: Boolean = {
    if (brokers.isEmpty || broker2topicInfo.isEmpty) {//如果topic下没有任何broker,或者topic没有任何心跳信息
      false
    }else {
      var score:Long = 0
      for((brokerId, topicHeartInfo) <- broker2topicInfo) {
        score = score + topicHeartInfo.getConsumeSum.get + topicHeartInfo.getProduceSum.get
      }
      score = score / brokers.size
      if (score >= 0 && score <= BusyTopicLevel.LEVEL) {
        false
      }else {
        true
      }
    }
  }

  def receive = {
    case createMail: CreateMail => {
      confirmAndUpdateStatus(createMail.id, createMail.from)
    }
    case StatusMail => {
      sender ! TopicInfo(self.path.name, brokers, clients)
    }
    case findLazyBrokersResMail: FindLazyBrokersResMail => {
      brokers ++= findLazyBrokersResMail.lazyBrokers
      actorSelection(findLazyBrokersResMail.client) ! FinishMail(self.path, brokers)
    }
    case deadMail: DeadMail => {
      deadMail.which match {
        case BROKER => {
          brokers -= deadMail.path
          for(clientPath <- clients) {
            actorSelection(clientPath) ! BrokerOffLine(deadMail.path.name)
          }
        }
        case CLIENT => {
          clients -= deadMail.path
        }
      }
    }
    case AreYouBusyNow => {
      if (iAmBusy){
        brokerRouter ! FindLazyBrokersMail(self.path, clients.toSeq: _*)//把client也一并传过去，查找完lazyBroker之后再创建
      }
    }
    case topicHeartInfoMail:TopicHeartInfoMail => {
      broker2topicInfo += (topicHeartInfoMail.brokerId -> topicHeartInfoMail.topicInfo)
    }
  }
}

object Topic {

  case class TopicInfo(val name: String, val brokers: Set[ActorPath], val clients: Set[ActorPath]) extends Info {}

}
