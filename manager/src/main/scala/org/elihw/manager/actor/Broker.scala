package org.elihw.manager.actor

import akka.actor.{ActorLogging, ActorRef, Actor}
import org.elihw.manager.mail.{BrokerHeartMail, FinishMail, FreshTopicsMail, RegisterMail}
import org.elihw.manager.communication.BrokerServerHandler
import scala.collection.JavaConversions.asScalaBuffer
import akka.actor.Status.Success
import com.jd.bdp.whale.common.command.TopicHeartInfo

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午7:50
 */
class Broker extends Actor with ActorLogging{

  var topicMap: Map[String, ActorRef] = Map()
  var handler: BrokerServerHandler = null
  var consumeMsgSec:Long = 0L
  var produceMsgSec:Long = 0L
  var topicHeartInfos:List[TopicHeartInfo] = null

  def receive = {
    case registerMail: RegisterMail => {
      handler = registerMail.handler
      val topicRouter = context.actorSelection("/user/manager/topicRouter")
      //转换java.util.list为inmutable.list
      var topicList = List[String]()
      for (topicName <- registerMail.cmd.getTopics) {
        topicList :+= topicName
      }
      //根据broker自带的topic刷新所有topic
      topicRouter ! FreshTopicsMail(topicList, registerMail.cmd.getId, self)
      log.info("broker注册成功！注册信息为:{}", registerMail.cmd)
    }
    case brokerHeartMail:BrokerHeartMail => {
      val cmd = brokerHeartMail.cmd
      produceMsgSec = cmd.getProduceMsgSec
      consumeMsgSec = cmd.getConsumerMsgSec
      topicHeartInfos = List[TopicHeartInfo]()
      for(topicHeartInfo <- cmd.getBrokerHeartInfos){
        topicHeartInfos :+= topicHeartInfo
      }
      log.debug("收到心跳信息{}", cmd)
    }
    case finishMail: FinishMail => {
      topicMap += (finishMail.topicName -> finishMail.topic)
    }
    case response:Success => {
      handler finishRegister(self)
    }
  }
}
