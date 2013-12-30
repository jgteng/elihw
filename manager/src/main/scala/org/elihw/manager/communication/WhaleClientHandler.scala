package org.elihw.manager.communication

import com.jd.bdp.whale.communication.{ServerWorkerHandler, TransportConnection_Thread}
import akka.actor.{PoisonPill, ActorRef}
import com.jd.bdp.whale.communication.message.Message
import com.jd.bdp.whale.common.communication.{ClientRegisterResponse, CommonResponse, MessageType}
import com.jd.dd.glowworm.PB
import com.jd.bdp.whale.common.command.{AllBrokersOfClientCmd, RegisterClientCmd, PublishTopicReqCmd}
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
import java.io.IOException

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:02
 */
class WhaleClientHandler(val connection: TransportConnection_Thread, val clientRouter: ActorRef, val clientRecheckTime: Int) extends ClientServerHandler {
  import Mail._

  val log: Logger = LoggerFactory.getLogger(classOf[WhaleClientHandler])

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

  val ON_LINE = true
  val OFF_LINE = false

  def notifyBrokerOnLine(brokerId:String, topicName:String) = {
    notifyBrokerInfo(brokerId, topicName, ON_LINE)
  }

  def notifyBrokerOffLine(brokerId:String, topicName:String) = {
    notifyBrokerInfo(brokerId, topicName, OFF_LINE)
  }

  def notifyBrokerInfo(brokerId:String, topicName:String, category:Boolean) = {
    val set = new util.TreeSet[Broker]()
    val broker = new Broker
    broker.setId(Integer.parseInt(brokerId))
    set.add(broker)
    val cmd = new AllBrokersOfClientCmd(topicName, set, category)
    val message = new Message(MessageType.ALL_BROKERS_OF_CLIENT_REQ, PB.toPBBytes(cmd), false)
    try {
      connection.sendMsg(message)
      log.info("broker:{},{}, 向client:{}推送，成功", brokerId, if(category == ON_LINE) "上线" else "下线", client.path.name)
    }catch {
      case e:IOException => {
        log.error("broker:{},{}, 向client:{}推送，失败", brokerId, if(category == ON_LINE) "上线" else "下线", client.path.name)
        log.error("异常信息为", e)
      }
    }
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
