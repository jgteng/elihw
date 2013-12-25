package org.elihw.manager.mail

import org.elihw.manager.communication.{ClientServerHandler, BrokerServerHandler}
import com.jd.bdp.whale.common.command.{RegisterClientCmd, PublishTopicReqCmd, HeartOfBrokerCmd, RegisterBrokerReqCmd}
import akka.actor.ActorPath
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.other.Info

/**
 * User: bigbully
 * Date: 13-10-29
 * Time: 下午10:31
 */
sealed trait Mail {}

sealed trait BrokerMail extends Mail {}

sealed trait TopicMail extends Mail {}

sealed trait ClientMail extends Mail {}

case class DeadMail(val which: String, val path:ActorPath) extends Mail

case class StatusResMail(val which: String, val list: List[Info]) extends Mail

case class RegisterBroekrMail(val cmd: RegisterBrokerReqCmd, val handler: BrokerServerHandler) extends BrokerMail

case class BrokerStatusMail(val includeLazyLavel:Boolean) extends BrokerMail

case class BrokerHeartMail(val cmd: HeartOfBrokerCmd) extends BrokerMail

case class CreateTopicMail(val topic: ActorPath) extends BrokerMail

case class FindLazyBrokersMail(val client:ActorPath, val topic:ActorPath) extends BrokerMail

case class FindLazyBrokersResMail(val lazyBrokers:Set[ActorPath], val client:ActorPath) extends BrokerMail

case class PublishTopicsMail(val topicList: List[String], val from: String) extends TopicMail

case class CreateMail(val topicName: String, val id: String, val from: String) extends TopicMail

case class FinishMail(val topic: ActorPath, val brokers: Set[ActorPath]) {
  def this(topic: ActorPath) = {
    this(topic, null)
  }
}

object FinishMail {
  def apply(topic: ActorPath): FinishMail = {
    new FinishMail(topic);
  }
}

case class BrokerOfTopicReqMail(val clientId: String) extends TopicMail

case class BrokerOfTopicResMail(val brokerInfos: Set[BrokerInfo]) extends TopicMail

case class PublishMail(val cmd: PublishTopicReqCmd, val handler: ClientServerHandler) extends ClientMail

case class RegisterClientMail(val cmd: RegisterClientCmd, val handler: ClientServerHandler) extends ClientMail

case class StartManagerMail(val baseDir: String) extends Mail {
  override def toString: String = "baseDir:" + baseDir
}

object Mail extends Enumeration {
  val CONSUMER = "consumer"
  val PRODUCER = "producer"
  val BROKER = "broker"
  val CLIENT = "client"
  val TOPIC = "topic"

  //mail
  val DisconnectMail = "Disconnect"
  val IsConnectedMail = "IsConnected"
  val StatusMail = "Status"
}


