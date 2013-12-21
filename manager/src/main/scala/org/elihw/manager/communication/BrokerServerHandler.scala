package org.elihw.manager.communication


import com.jd.bdp.whale.communication.{TransportConnection_Thread, ServerWorkerHandler}
import com.jd.bdp.whale.communication.message.Message
import com.jd.bdp.whale.common.communication.{CommonResponse, MessageType}
import com.jd.dd.glowworm.PB
import com.jd.bdp.whale.common.command.{HeartOfBrokerCmd, RegisterBrokerReqCmd}
import akka.actor.{ActorPath, PoisonPill, ActorLogging, ActorRef}
import org.elihw.manager.mail._
import java.util.{TimerTask, Timer}
import org.elihw.manager.mail.BrokerHeartMail
import org.elihw.manager.mail.RegisterBroekrMail
import org.elihw.manager.mail.DisconnectMail
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

  val log:Logger = LoggerFactory.getLogger(classOf[BrokerServerHandler])

  implicit val timeout = Timeout(1 seconds)
  
  var broker: ActorRef = null

  def transportOnException(p1: Exception) = {
    log.error("与broker端通信发生异常,等待重连")
    signDisconnection
  }

  private def signDisconnection = {
    broker ! DisconnectMail(broker.path.name)
    val timer: Timer = new Timer
    timer.schedule(new CheckTimerTask, brokerRecheckTime)
  }

  def doMsgHandler(message: Message): Message = {
    message.getMsgType match {
      case MessageType.REGISTER_BROKER_REQ => {
        val registerBrokerReqCmd = PB.parsePBBytes(message.getContent).asInstanceOf[RegisterBrokerReqCmd]
        if (registerBrokerReqCmd.getIp == null){
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
      val isConnected = Await.result(broker ? IsConnectedMail(broker.path.name), timeout.duration).asInstanceOf[Boolean]
      if (isConnected){

      }
      val brokerId = broker.path.name
      destory
      log.error("与brokerId:{}通信彻底断开!", brokerId)
      this.cancel
    }

  }

}

