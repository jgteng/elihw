package org.elihw.manager

import akka.actor._
import org.ini4j.{Profile, Ini}
import java.io.File
import com.jd.bdp.whale.communication.{ServerWorkerHandler, TransportConnection_Thread, ServerWorkerHandlerFactory, ServerNIO}
import org.elihw.manager.communication.{WhaleClientHandler, WhaleBrokerHandler}
import org.elihw.manager.actor.{CycleCheck, ClientRouter, TopicRouter, BrokerRouter}
import org.elihw.manager.mail.{CycleTaskMail, StatusResMail, Mail, StartManagerMail}
import java.util.{TimerTask, Timer}
import akka.util.Timeout
import scala.concurrent.duration._
import Mail._
import org.elihw.manager.actor.Topic.TopicInfo
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.actor.Client.ClientInfo
import org.elihw.manager.other.{BrokerScale, LazyBrokerLevel, BusyTopicLevel}

/**
 * User: bigbully
 * Date: 13-10-29
 * Time: 下午10:11
 */
object Manager {

  def main(args: Array[String]) {
    val system = ActorSystem("manager")
    val manager = system.actorOf(Props[Manager], "manager")
    val baseDir = args(0);
    manager ! StartManagerMail(baseDir) //开启manager服务
  }

}

class Manager extends Actor with ActorLogging {

  implicit val timeout = Timeout(2 seconds)

  var brokerRouter: ActorRef = null
  var topicRouter: ActorRef = null
  var clientRouter: ActorRef = null

  var toBrokerServer: ServerNIO = null
  var toClientServer: ServerNIO = null

  def initManagerServer(baseDir: String) = {
    import context._
    val file = new File(baseDir + "/manager.ini")
    val ini = new Ini(file)
    val portSection: Profile.Section = ini.get("port")
    val toClientPort: Int = Integer.parseInt(portSection.get("client_port"))
    val toBrokerPort: Int = Integer.parseInt(portSection.get("broker_port"))

    val managerSection: Profile.Section = ini.get("manager")
    val brokerRecheckTime = Integer.parseInt(managerSection.get("broker_recheck_time"))
    val clientRecheckTime = Integer.parseInt(managerSection.get("client_recheck_time"))    

    
    //以下是可变属性
    val sleepTimeStr = managerSection.get("sleep_time")
    var sleepTime = 0
    if (sleepTimeStr != null) sleepTime = Integer.parseInt(sleepTimeStr)

    val maxBrokerOfTopicStr = managerSection.get("max_broker_of_topic")
    if (maxBrokerOfTopicStr != null) BrokerScale.MAX_BROKER_OF_TOPIC = Integer.parseInt(maxBrokerOfTopicStr)

    val maxTopicInABrokerStr = managerSection.get("max_topic_in_a_broker")
    if (maxTopicInABrokerStr != null) BrokerScale.MAX_TOPIC_IN_A_BROKER = Integer.parseInt(maxTopicInABrokerStr)

    val busyLevelStr = managerSection.get("busy_level")
    if (busyLevelStr != null) BusyTopicLevel.LEVEL = Integer.parseInt(busyLevelStr)
    
    val lazyLevel1Str = managerSection.get("lazy_level1")
    if (lazyLevel1Str != null) LazyBrokerLevel.LEVEL1 = Integer.parseInt(lazyLevel1Str)

    val lazyLevel2Str = managerSection.get("lazy_level1")
    if (lazyLevel2Str != null) LazyBrokerLevel.LEVEL2 = Integer.parseInt(lazyLevel2Str)

    val lazyLevel3Str = managerSection.get("lazy_level1")
    if (lazyLevel3Str != null) LazyBrokerLevel.LEVEL3 = Integer.parseInt(lazyLevel3Str)


    val busyTopicCheckTimeStr = managerSection.get("busy_topic_check_time")
    if (busyTopicCheckTimeStr != null){
      topicRouter ! StartBusyTopicCheck
    }

    //三个router不针对指定id的broker,topic,client操作的话，都走router
    brokerRouter = actorOf(Props[BrokerRouter], "brokerRouter")
    topicRouter = actorOf(Props[TopicRouter], "topicRouter")
    clientRouter = actorOf(Props[ClientRouter], "clientRouter")

    toBrokerServer = new ServerNIO(toBrokerPort, new ServerWorkerHandlerFactory() {
      def createServerWorkerHandler(connection: TransportConnection_Thread): ServerWorkerHandler = {
        new WhaleBrokerHandler(connection, brokerRouter, brokerRecheckTime)
      }
    })
    toBrokerServer.start
    log.info("broker-server启动完成")
    log.info("broker启动后暂停{}秒", sleepTime / 1000)
    Thread.sleep(sleepTime)

    toClientServer = new ServerNIO(toClientPort, new ServerWorkerHandlerFactory() {
      def createServerWorkerHandler(connection: TransportConnection_Thread): ServerWorkerHandler = {
        log.debug("新建一个clientHandler")
        new WhaleClientHandler(connection, clientRouter, clientRecheckTime)
      }
    })
    toClientServer.start
    log.info("client-server启动完成")

    //启动测试timer
    val timer = new Timer
    timer.schedule(new TestTimerTask, 0, 10000)
  }

  def receive: Actor.Receive = {
    case startMail: StartManagerMail => {
      initManagerServer(startMail.baseDir)
    }
    case statusResMail: StatusResMail => {
      statusResMail.which match {
        case BROKER => {
          val vector = for (info <- statusResMail.list) yield {
            val brokerInfo = info.asInstanceOf[BrokerInfo]
            (brokerInfo.id, brokerInfo.ip, brokerInfo.port,
              for (topic <- brokerInfo.topics) yield {
                topic.name
              })
          }
          println(BROKER + ":" + vector)
        }
        case CLIENT => {
          val vector = for (info <- statusResMail.list) yield {
            val clientInfo = info.asInstanceOf[ClientInfo]
            (clientInfo.id, clientInfo.category, clientInfo.group, clientInfo.topic.name,
              for (broker <- clientInfo.brokers) yield {
                broker.name
              }
              )
          }
          println(CLIENT + ":" + vector)
        }
        case TOPIC => {
          val vector = for (info <- statusResMail.list) yield {
            val topicInfo = info.asInstanceOf[TopicInfo]
            (topicInfo.name,
              for (broker <- topicInfo.brokers) yield {
                broker.name
              },
              for (client <- topicInfo.clients) yield {
                client.name
              }
              )
          }
          println(TOPIC + ":" + vector)
        }
      }
    }
  }

  class TestTimerTask extends TimerTask {

    def run() = {
      brokerRouter ! StatusMail
      clientRouter ! StatusMail
      topicRouter ! StatusMail
    }

  }

}


