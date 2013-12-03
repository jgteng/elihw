package org.elihw.manager.actor

import akka.actor.{ActorLogging, ActorRef, Actor}
import org.elihw.manager.mail._
import org.elihw.manager.communication.BrokerServerHandler
import scala.collection.JavaConversions.asScalaBuffer
import com.jd.bdp.whale.common.command.TopicHeartInfo
import org.elihw.manager.mail.BrokerHeartMail
import org.elihw.manager.mail.FinishMail
import akka.actor.Status.Success
import org.elihw.manager.mail.RegisterMail

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午7:50
 */
class Broker(val handler:BrokerServerHandler, val baseInfo:BaseInfo) extends Actor with ActorLogging {

  var topicMap: Map[String, ActorRef] = Map()
  var clientMap: Map[String, ActorRef] = Map()
  var consumeMsgSec: Long = 0L
  var produceMsgSec: Long = 0L
  var topicHeartInfos: List[TopicHeartInfo] = null

  def receive = {
    case registerMail: RegisterMail => {
      val cmd = registerMail.cmd
      val topicRouter = context.actorSelection("/user/manager/topicRouter")
      //转换java.util.list为inmutable.list
      var topicList = List[String]()
      for (topicName <- cmd.getTopics) {
        topicList :+= topicName
      }

      //根据broker自带的topic刷新所有topic
      topicRouter ! PublishTopicsMail(topicList, Mail.BROKER)
      log.info("broker注册成功！注册信息为:{}", cmd)
    }
    case brokerHeartMail: BrokerHeartMail => {
      val cmd = brokerHeartMail.cmd
      produceMsgSec = cmd.getProduceMsgSec
      consumeMsgSec = cmd.getConsumerMsgSec
      topicHeartInfos = List[TopicHeartInfo]()
      for (topicHeartInfo <- cmd.getBrokerHeartInfos) {
        topicHeartInfos :+= topicHeartInfo
      }
      log.debug("收到心跳信息{}", cmd)
    }
    case finishMail: FinishMail => {
      topicMap += (finishMail.topicName -> finishMail.topic)
    }
    case response: Success => {
      handler finishRegister (self)
    }
    case baseInfoMail: BaseInfoMail => {
      sender ! baseInfo
    }
  }
}

class BaseInfo(val id: Int, val ip: String, val port: Int) {}
object BaseInfo {
  def apply(id: Int, ip: String, port: Int):BaseInfo = {
    new BaseInfo(id, ip, port)
  }
}
