package org.elihw.manager.mail

import org.elihw.manager.communication.{ClientServerHandler, BrokerServerHandler}
import com.jd.bdp.whale.common.command.{PublishTopicReqCmd, HeartOfBrokerCmd, RegisterBrokerReqCmd}
import akka.actor.ActorRef
import akka.actor.Status.Success
import org.elihw.manager.actor.BaseInfo
import org.elihw.manager.Util.Model

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

case class BuildInTopicsMail(val topicList:List[String]) extends TopicMail

case class PublishTopicMail(val topicName:String) extends TopicMail

case class PublishTopicsMail(val topicList:List[String], implicit val model:Model) extends TopicMail

object PublishTopicsMail {
  def apply(topicList:List[String])(implicit model:Model) = {
    new PublishTopicsMail(topicList, model)
  }
}

case class CreateMail(val topicName:String, val creator:ActorRef, val model:Model) extends TopicMail

object CreateMail {
  def apply(topicName:String, creator:ActorRef)(implicit model:Model):CreateMail = {
    new CreateMail(topicName, creator, model)
  }
}

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
}

