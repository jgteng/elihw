package org.elihw.manager.communication

import com.jd.bdp.whale.communication.{ServerWorkerHandler, TransportConnection_Thread}
import akka.actor.ActorRef
import com.jd.bdp.whale.communication.message.Message
import com.jd.bdp.whale.common.communication.{CommonResponse, MessageType}
import com.jd.dd.glowworm.PB
import com.jd.bdp.whale.common.command.{PublishTopicReqCmd, HeartOfBrokerCmd, RegisterBrokerReqCmd}
import org.elihw.manager.mail.{PublishMail, BrokerHeartMail, RegisterMail}

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:02
 */
class ClientServerHandler (val connection: TransportConnection_Thread, val clientRouter:ActorRef) extends ServerWorkerHandler{

  var client:ActorRef = null

  def finishRegister(client: ActorRef, brokerMap:Map[String, ActorRef]) = {
    this.client = client

    val result = new Message
    result.setMsgType(MessageType.CONNECT_MANAGER_SUCCESS)
    result.setContent(PB.toPBBytes(CommonResponse.successResponse()))
    connection.sendMsg(result)
  }


  def doMsgHandler(message: Message): Message = {
    message.getMsgType match {
      case MessageType.PUBLISH_TOPIC_REQ => {
        val cmd = PB.parsePBBytes(message.getContent).asInstanceOf[PublishTopicReqCmd]
        clientRouter ! PublishMail(cmd, this)
      }
      case _ => println("client不支持的类型")
    }
    val result = new Message
    result.setContent(PB.toPBBytes(CommonResponse.successResponse))
    result
  }

  def transportOnException(p1: Exception) = {

  }
}
