package org.elihw.manager.communication


import com.jd.bdp.whale.communication.{TransportConnection_Thread, ServerWorkerHandler}
import com.jd.bdp.whale.communication.message.{MsgMarshallerFactory, Message}
import com.jd.bdp.whale.common.communication.{CommonResponse, MessageType}
import com.jd.dd.glowworm.PB
import com.jd.bdp.whale.common.command.{PublishTopicReqCmd, HeartOfBrokerCmd, RegisterBrokerReqCmd}
import akka.actor.{PoisonPill, ActorRef}
import java.util.{TimerTask, Timer}
import org.elihw.manager.mail.{Mail, BrokerHeartMail, RegisterBroekrMail}
import akka.pattern._
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 上午10:33
 */
class BrokerServerHandler(val connection: TransportConnection_Thread, val brokerRouter: ActorRef, val brokerRecheckTime: Int) extends ServerWorkerHandler {
  import Mail._

  val log: Logger = LoggerFactory.getLogger(classOf[BrokerServerHandler])

  implicit val timeout = Timeout(1 seconds)

  var broker: ActorRef = null

  def transportOnException(p1: Exception) = {
    log.error("与broker端通信发生异常, brokerId:{}, 等待重连", broker.path.name)
    signDisconnection
  }

  def signDisconnection = {
    broker ! DisconnectMail
    val timer: Timer = new Timer
    timer.schedule(new CheckTimerTask, brokerRecheckTime)
  }

  def doMsgHandler(message: Message): Message = {
    message.getMsgType match {
      case MessageType.REGISTER_BROKER_REQ => {
        val registerBrokerReqCmd = PB.parsePBBytes(message.getContent).asInstanceOf[RegisterBrokerReqCmd]
        if (registerBrokerReqCmd.getIp == null) {
          registerBrokerReqCmd.setIp(connection.getRemoteIpAddr)
        }
        brokerRouter ! RegisterBroekrMail(registerBrokerReqCmd, this)
      }
      case MessageType.BROKER_HEART => {
        val heartOfBrokerCmd = PB.parsePBBytes(message.getContent).asInstanceOf[HeartOfBrokerCmd]
        broker ! BrokerHeartMail(heartOfBrokerCmd)
      }
      case _ => println("broker不支持的类型")
    }

    val result = new Message
    result.setContent(PB.toPBBytes(CommonResponse.successResponse))
    result
  }

  def createTopic(topicName:String):Boolean = {
    val message: Message = new Message(MessageType.PUBLISH_TOPIC_REQ, PB.toPBBytes(new PublishTopicReqCmd(topicName)), true)
    try {
      val result = connection.sendMsg(message, 2000)
      if (result.getMsgType == MsgMarshallerFactory.TransportExceptionResponse_MsgType) {
        log.error("与brokerId为:{}的broker在注册topic失败！", broker.path.name)
        false
      }else {
        val commonResponse: CommonResponse = PB.parsePBBytes(result.getContent).asInstanceOf[CommonResponse]
        if (commonResponse.getType == CommonResponse.EXCEPTION_TYPE) {
          log.error("与brokerId为:{}的broker在注册topic失败！", broker.path.name)
          false
        }else {
          true
        }
      }
    }catch {
      case e:Exception => {
        log.error("与brokerId为:{}的broker通信失败，注册topic失败！", broker.path.name)
        false
      }
    }
  }

  def finishRegister(broker: ActorRef) = {
    this.broker = broker
    val result = new Message
    result.setMsgType(MessageType.CONNECT_MANAGER_SUCCESS)
    result.setContent(PB.toPBBytes(CommonResponse.successResponse))
    connection.sendMsg(result)
  }

  def destory = {
    broker ! PoisonPill
  }

  class CheckTimerTask extends TimerTask {
    def run = {
      val isConnected = Await.result(broker ? IsConnectedMail, timeout.duration).asInstanceOf[Boolean]
      if (!isConnected) {
        val brokerId = broker.path.name
        destory
        log.error("与brokerId:{}通信彻底断开!", brokerId)
        this.cancel
      }
    }
  }

}

