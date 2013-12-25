package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail._
import org.elihw.manager.communication.BrokerServerHandler
import scala.collection.JavaConversions.asScalaBuffer
import com.jd.bdp.whale.common.command.TopicHeartInfo
import org.elihw.manager.mail.BrokerHeartMail
import org.elihw.manager.mail.FinishMail
import akka.actor.Status.Success
import org.elihw.manager.mail.RegisterBroekrMail
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.other.{LazyBrokerLevel, Info}
import org.elihw.manager.mail.RegisterBroekrMail
import org.elihw.manager.mail.BrokerHeartMail
import org.elihw.manager.mail.CreateTopicMail
import org.elihw.manager.mail.PublishTopicsMail
import akka.actor.Status.Success
import org.elihw.manager.mail.BrokerStatusMail

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午7:50
 */
class Broker(var handler:BrokerServerHandler, val brokerInfo:BrokerInfo) extends Actor with ActorLogging {

  import Mail._
  import context._

  var topics: Set[ActorPath] = Set()
  var consumeMsgSec: Long = 0L
  var produceMsgSec: Long = 0L
  var topicHeartInfos: List[TopicHeartInfo] = null
  var isConnected = true

  def lazyLevel:Int = {
    import LazyBrokerLevel._
    val topicHeartInfos = this.topicHeartInfos
    if (topicHeartInfos == null){
      HIGH
    }else {
      var score:Long = 0L
      for(topicHeartInfo <- topicHeartInfos){
        score += (topicHeartInfo.getConsumeSum.get + topicHeartInfo.getProduceSum.get)
      }
      if (score >= 0 && score < LEVEL1){
        LOW
      }else if (score >= LEVEL1 && score < LEVEL2){
        NORMAL
      }else {
        HIGH
      }
    }
  }

  def receive = {
    case registerMail: RegisterBroekrMail => {
      if (!isConnected) {//证明是重连,替换handler
        handler = registerMail.handler
        handler.finishRegister(self)
        log.info("Broker:{}重连成功", registerMail.cmd.getId)
      }else {
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
    case DisconnectMail => {
      isConnected = false
    }
    case IsConnectedMail => {
      sender ! isConnected
    }
    case response: Success => {
      handler finishRegister (self)
    }
    case brokerStatusMail: BrokerStatusMail => {
      sender ! BrokerInfo(brokerInfo, topics, lazyLevel)
    }
    case StatusMail => {
      sender ! BrokerInfo(brokerInfo, topics)
    }
    case createTopicMail: CreateTopicMail =>{
      topics += createTopicMail.topic
      val result = handler createTopic createTopicMail.topic.name
      //todo 如果result = false如何处理
    }
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    val brokerPath = self.path
    for (topicPath <- topics){
      actorSelection(topicPath) ! DeadMail(BROKER, brokerPath)
    }
  }
}

object Broker {
  case class BrokerInfo(val id: Int, val ip: String, val port: Int) extends Info{

    var topics:Set[ActorPath] = null
    var lazyLevl:Int = 0

    def this(id: Int, ip: String, port: Int, topics: Set[ActorPath]) = {
      this(id, ip, port)
      this.topics = topics
    }

    def this(id: Int, ip: String, port: Int, topics: Set[ActorPath], lazyLevl:Int) = {
      this(id, ip, port, topics)
      this.lazyLevl = lazyLevl
    }
    override def toString: String = "id:" + id + ",ip:" + ip + ",port:" + port
  }

  object BrokerInfo {
    def apply(brokerInfo: BrokerInfo, topics: Set[ActorPath]) = {
      new BrokerInfo(brokerInfo.id, brokerInfo.ip, brokerInfo.port, topics)
    }
    def apply(brokerInfo: BrokerInfo, topics: Set[ActorPath], lazyLevel:Int) = {
      new BrokerInfo(brokerInfo.id, brokerInfo.ip, brokerInfo.port, topics, lazyLevel)
    }
  }
}


