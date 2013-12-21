package org.elihw.manager.actor

import akka.actor.{ActorPath, ActorLogging, ActorRef, Actor}
import org.elihw.manager.mail._
import org.elihw.manager.communication.BrokerServerHandler
import scala.collection.JavaConversions.asScalaBuffer
import com.jd.bdp.whale.common.command.TopicHeartInfo
import org.elihw.manager.mail.BrokerHeartMail
import org.elihw.manager.mail.FinishMail
import akka.actor.Status.Success
import org.elihw.manager.mail.RegisterBroekrMail
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.other.Info

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午7:50
 */
class Broker(val handler:BrokerServerHandler, val brokerInfo:BrokerInfo) extends Actor with ActorLogging {

  var topics: Set[ActorPath] = Set()
  var consumeMsgSec: Long = 0L
  var produceMsgSec: Long = 0L
  var topicHeartInfos: List[TopicHeartInfo] = null
  var isConnected = true

  def receive = {
    case registerMail: RegisterBroekrMail => {
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
      handler.finishRegister(self)
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
      topics += finishMail.topic
    }
    case disconnectMail: DisconnectMail => {
      isConnected = false
    }
    case isConnectMail: IsConnectedMail => {
      sender ! isConnected
    }
    case response: Success => {
      handler finishRegister (self)
    }
    case statusMail: StatusMail => {
      sender ! BrokerInfo(brokerInfo, topics)
    }
  }
}

object Broker {
  case class BrokerInfo(val id: Int, val ip: String, val port: Int) extends Info{

    var topics:Set[ActorPath] = null

    def this(id: Int, ip: String, port: Int, topics: Set[ActorPath]) = {
      this(id, ip, port)
      this.topics = topics
    }
    override def toString: String = "id:" + id + ",ip:" + ip + ",port:" + port
  }

  object BrokerInfo {
    def apply(brokerInfo: BrokerInfo, topics: Set[ActorPath]) = {
      new BrokerInfo(brokerInfo.id, brokerInfo.ip, brokerInfo.port, topics)
    }
  }
}


