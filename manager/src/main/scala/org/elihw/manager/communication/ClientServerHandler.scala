package org.elihw.manager.communication

import com.jd.bdp.whale.communication.{ServerWorkerHandler, TransportConnection_Thread}
import akka.actor.{PoisonPill, ActorRef}
import com.jd.bdp.whale.communication.message.Message
import com.jd.bdp.whale.common.communication.{ClientRegisterResponse, CommonResponse, MessageType}
import com.jd.dd.glowworm.PB
import com.jd.bdp.whale.common.command.{RegisterClientCmd, PublishTopicReqCmd}
import org.elihw.manager.mail.{Mail, RegisterClientMail, PublishMail}
import com.jd.bdp.whale.common.model.Broker
import java.util
import org.elihw.manager.actor.Broker.BrokerInfo
import org.slf4j.{LoggerFactory, Logger}
import java.util.{TimerTask, Timer}
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern._

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:02
 */
class ClientServerHandler(val connection: TransportConnection_Thread, val clientRouter: ActorRef, val clientRecheckTime: Int) extends ServerWorkerHandler {
  import Mail._

  val log: Logger = LoggerFactory.getLogger(classOf[ClientServerHandler])

  var client: ActorRef = null
  implicit val timeout = Timeout(1 seconds)
  implicit def baseInfo2Broker(baseInfo: BrokerInfo): Broker = {
    new Broker(baseInfo.id, baseInfo.ip, baseInfo.port, true)
  }

  private def assemble(ref: ActorRef, topicName: String, set: Set[BrokerInfo]): ClientRegisterResponse = {
    val response = new ClientRegisterResponse
    val brokers: java.util.TreeSet[Broker] = new util.TreeSet[Broker]()
    for (baseInfo <- set) {
      brokers.add(baseInfo)
    }
    response.setBrokers(brokers)
    response.setClientId(client.path.name)
    response.setResult(true)
    response.setTopicName(topicName)
    response
  }

  def finishRegister(client: ActorRef, topicName: String, baseInfos: Set[BrokerInfo]) = {
    this.client = client
    val result = new Message
    result.setMsgType(MessageType.CONNECT_MANAGER_SUCCESS)
    val response = assemble(this.client, topicName, baseInfos)
    result.setContent(PB.toPBBytes(response))
    connection.sendMsg(result)
    log.debug("返回注册信息：clientId:{}, brokers:{}, topic:{}", this.client.path.name, baseInfos, topicName)
  }


  def doMsgHandler(message: Message): Message = {
    message.getMsgType match {
      case MessageType.PUBLISH_TOPIC_REQ => {
        val cmd = PB.parsePBBytes(message.getContent).asInstanceOf[PublishTopicReqCmd]
        clientRouter ! PublishMail(cmd, this)
      }
      case MessageType.REGISTER_CLIENT => {
        val cmd = PB.parsePBBytes(message.getContent).asInstanceOf[RegisterClientCmd]
        clientRouter ! RegisterClientMail(cmd, this)
      }
      case _ => println("client不支持的类型")
    }
    val result = new Message
    result.setContent(PB.toPBBytes(CommonResponse.successResponse))
    result
  }

  def transportOnException(p1: Exception) = {
    log.error("与client端通信发生异常,clientId:{},等待重连", client.path.name)
    signDisconnection
  }

  def signDisconnection = {
    client ! DisconnectMail
    val timer: Timer = new Timer
    timer.schedule(new CheckTimerTask, clientRecheckTime)
  }

  def destory = {
    client ! PoisonPill
  }

  class CheckTimerTask extends TimerTask {
    def run = {
      val isConnected = Await.result(client ? IsConnectedMail, timeout.duration).asInstanceOf[Boolean]
      if (!isConnected) {
        val clientId = client.path.name
        destory
        log.error("与clientId:{}通信彻底断开!", clientId)
        this.cancel
      }
    }

  }

}
