package org.elihw.manager.communication

import com.jd.bdp.whale.communication.{TransportConnection_Thread, ServerWorkerHandler}
import com.jd.bdp.whale.communication.message.Message
import com.jd.bdp.whale.common.communication.{CommonResponse, MessageType}
import com.jd.dd.glowworm.PB
import com.jd.bdp.whale.common.command.RegisterBrokerReqCmd
import akka.actor.ActorSystem
import org.elihw.manager.mail.RegisterMail

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 上午10:33
 */
class BrokerServerHandler(val connection: TransportConnection_Thread) extends ServerWorkerHandler {

  def transportOnException(p1: Exception) = {

  }

  def doMsgHandler(message: Message): Message = {
    val result = new Message
    message.setContent(PB.toPBBytes(CommonResponse.successResponse))
    message.getMsgType match {
      case MessageType.REGISTER_BROKER_REQ => {
        val registerBrokerReqCmd = PB.parsePBBytes(message.getContent).asInstanceOf[RegisterBrokerReqCmd]
        ActorSystem("manager").actorSelection("manager/brokerRouter") ! RegisterMail(registerBrokerReqCmd, this)
      }
      case _ => println("暂不支持这种类型")
    }
    result
  }

  def finishRegister = {
    val result = new Message
    result.setMsgType(MessageType.CONNECT_MANAGER_SUCCESS)
    result.setContent(PB.toPBBytes(CommonResponse.successResponse))
    connection.sendMsg(result)
  }

}
