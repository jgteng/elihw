package org.elihw.manager.communication

import com.jd.bdp.whale.communication.{TransportConnection_Thread, ServerWorkerHandler}
import com.jd.bdp.whale.communication.message.Message
import com.jd.bdp.whale.common.communication.{CommonResponse, MessageType}
import com.jd.dd.glowworm.PB
import com.jd.bdp.whale.common.command.RegisterBrokerReqCmd
import akka.actor.ActorSystem
import org.elihw.manager.mail.RegisterMail
import akka.pattern.ask
import akka.actor.Status.{Failure, Success, Status}
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 上午10:33
 */
class BrokerServerHandler(connection: TransportConnection_Thread) extends ServerWorkerHandler {


  implicit val timeout = Timeout(1 seconds)

  def transportOnException(p1: Exception) = {

  }

  def doMsgHandler(message: Message): Message = {
    val result: Message = new Message()
    message.setContent(PB.toPBBytes(CommonResponse.successResponse))
    message.getMsgType match {
      case MessageType.REGISTER_BROKER_REQ => {
        val registerBrokerReqCmd = PB.parsePBBytes(message.getContent).asInstanceOf[RegisterBrokerReqCmd]
        val future = ActorSystem("manager").actorSelection("manager/brokerRouter") ? RegisterMail(registerBrokerReqCmd, this)
        future.mapTo[Status] match {
          case Success => {
            result.setContent(PB.toPBBytes(CommonResponse.successResponse))
          }
          case Failure => {
            result.setContent(PB.toPBBytes(CommonResponse.exceptionResponse("broker注册失败")))
          }
        }
      }
      case _ => println("暂不支持这种类型")
    }
    result
  }


}
