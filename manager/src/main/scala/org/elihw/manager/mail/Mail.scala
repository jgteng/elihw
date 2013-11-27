package org.elihw.manager.mail

import org.elihw.manager.communication.{ClientServerHandler, BrokerServerHandler}
import com.jd.bdp.whale.common.command.{PublishTopicReqCmd, HeartOfBrokerCmd, RegisterBrokerReqCmd}
import akka.actor.ActorRef
import akka.actor.Status.Success

/**
 * User: bigbully
 * Date: 13-10-29
 * Time: 下午10:31
 */
sealed trait Mail{}

sealed trait BrokerMail extends Mail{}
sealed trait TopicMail extends Mail{}
sealed trait ClientMail extends Mail{}

case class RegisterMail(val cmd:RegisterBrokerReqCmd, val handler:BrokerServerHandler) extends BrokerMail {}

case class BrokerHeartMail(val cmd:HeartOfBrokerCmd) extends BrokerMail{}

case class FreshTopicsMail(val topicList:List[String],val brokerId:Int, val broker:ActorRef) extends TopicMail {}

case class CreateMail(val brokerId:Int, val broker:ActorRef) extends TopicMail {}

case class FinishMail(val topicName:String, val topic: ActorRef) extends TopicMail{}

case class PublishMail(val cmd:PublishTopicReqCmd, val handler:ClientServerHandler) extends ClientMail {}

case class StartManagerMail(val baseDir:String) extends Mail {
  override def toString:String = "baseDir:" + baseDir
}



