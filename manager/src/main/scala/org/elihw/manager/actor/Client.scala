package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail._
import org.elihw.manager.communication.ClientServerHandler
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.other.Info
import org.elihw.manager.actor.Client.ClientInfo
import org.elihw.manager.mail.Mail._
import org.elihw.manager.mail.RegisterClientMail
import org.elihw.manager.mail.PublishTopicsMail
import org.elihw.manager.mail.PublishMail

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:04
 */
abstract class Client(val handler: ClientServerHandler) extends Actor with ActorLogging {

  import context._

  implicit val timeout = Timeout(1 seconds)

  var topic: ActorPath = null
  var brokers: Set[ActorPath] = Set()
  var isConnected = true

  def receive = {
    case publishMail: PublishMail => {
      log.debug("client:{}注册topic:{}", publishMail.cmd.getClientId, publishMail.cmd.getTopicName)
      val topicRouter = context.actorSelection("/user/manager/topicRouter")
      topicRouter ! PublishTopicsMail(List(publishMail.cmd.getTopicName), Mail.CLIENT)
    }
    case registerMail: RegisterClientMail => {
      log.debug("client:{}注册topic:{}", registerMail.cmd.getClientId, registerMail.cmd.getTopicName)
      val topicRouter = context.actorSelection("/user/manager/topicRouter")
      topicRouter ! PublishTopicsMail(List(registerMail.cmd.getTopicName), Mail.CLIENT)
    }
    case finishMail: FinishMail => {
      val (topic, brokers) = (finishMail.topic, finishMail.brokers)
      this.topic = topic
      this.brokers = brokers
      handler finishRegister(self, topic.name, getBrokerBaseInfos(brokers))
      log.info("client注册成功！注册信息为:clientId:{}, topic:{}", self.path.name, topic.name)
    }
    case StatusMail => {
      sender ! getInfo
    }
    case DisconnectMail => {
      isConnected = false
    }
    case IsConnectedMail => {
      sender ! isConnected
    }
  }

  def getInfo: ClientInfo

  def getBrokerBaseInfos(brokers: Set[ActorPath]): Set[BrokerInfo] = {
    var baseInfos = Set[BrokerInfo]()
    for (path <- brokers) {
      val baseInfo = Await.result(actorSelection(path) ? StatusMail, timeout.duration).asInstanceOf[BrokerInfo]
      if (baseInfo != null) baseInfos += baseInfo
    }
    baseInfos
  }
}

object Client {

  case class ClientInfo(val id: String, val category: String, val topic: ActorPath, val brokers: Set[ActorPath]) extends Info {

    var group: String = null

    def this(id: String, category: String, group: String, topic: ActorPath, brokers: Set[ActorPath]) = {
      this(id, category, topic, brokers)
      this.group = group
    }

  }

  object ClientInfo {

    def apply(id: String, category: String, group: String, topic: ActorPath, brokers: Set[ActorPath]) = {
      new ClientInfo(id, category, group, topic, brokers)
    }
  }

}
