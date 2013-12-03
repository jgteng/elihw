package org.elihw.manager.mail

import org.elihw.manager.communication.{ClientServerHandler, BrokerServerHandler}
import com.jd.bdp.whale.common.command.{PublishTopicReqCmd, HeartOfBrokerCmd, RegisterBrokerReqCmd}
import akka.actor.ActorRef
import akka.actor.Status.Success
import org.elihw.manager.actor.BaseInfo

/**
 * User: bigbully
 * Date: 13-10-29
 * Time: 下午10:31
 */
sealed trait Mail{}

sealed trait BrokerMail extends Mail{}
sealed trait TopicMail extends Mail{}
sealed trait ClientMail extends Mail{}

case class RegisterMail(val cmd:RegisterBrokerReqCmd, val handler:BrokerServerHandler) extends BrokerMail

case class BrokerHeartMail(val cmd:HeartOfBrokerCmd) extends BrokerMail

case class BaseInfoMail(val id:Int) extends BrokerMail

case class PublishTopicsMail(val topicList:List[String], val from:Int) extends TopicMail

case class CreateMail(val topicName:String, val id:String, val from:Int) extends TopicMail

case class FinishMail(val topicName:String, val topic: ActorRef) extends TopicMail

case class BrokerOfTopicReqMail(val clientId:String) extends TopicMail

case class BrokerOfTopicResMail(val brokerMap:Map[String, ActorRef]) extends TopicMail

case class PublishMail(val cmd:PublishTopicReqCmd, val handler:ClientServerHandler) extends ClientMail

case class StartManagerMail(val baseDir:String) extends Mail {
  override def toString:String = "baseDir:" + baseDir
}

object Mail extends Enumeration {
  val CONSUMER = 0
  val PRODUCER = 1
  val BROKER = 2
  val CLIENT = 3
}

