package org.elihw.manager

import akka.actor._
import org.ini4j.{Profile, Ini}
import java.io.File
import com.jd.bdp.whale.communication.{ServerWorkerHandler, TransportConnection_Thread, ServerWorkerHandlerFactory, ServerNIO}
import org.elihw.manager.communication.{ClientServerHandler, BrokerServerHandler}
import org.elihw.manager.actor.{ClientRouter, TopicRouter, BrokerRouter}
import org.elihw.manager.mail.{StatusResMail, Mail, StartManagerMail}
import java.util.{TimerTask, Timer}
import akka.util.Timeout
import scala.concurrent.duration._
import Mail._
import org.elihw.manager.actor.Topic.TopicInfo
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.actor.Client.ClientInfo

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
    val file = new File(baseDir + "/manager.ini")
    val ini = new Ini(file)
    val portSection: Profile.Section = ini.get("port")
    val toClientPort: Int = Integer.parseInt(portSection.get("client_port"))
    val toBrokerPort: Int = Integer.parseInt(portSection.get("broker_port"))

    val managerSection: Profile.Section = ini.get("manager")
    val brokerRecheckTime = Integer.parseInt(managerSection.get("broker_recheck_time"))
    val clientRecheckTime = Integer.parseInt(managerSection.get("client_recheck_time"))

    val maxBrokerOfTopic = Integer.parseInt(managerSection.get("max_broker_of_topic"))
    val maxTopicInABroker = Integer.parseInt(managerSection.get("max_topic_in_a_broker"))

    brokerRouter = context.actorOf(Props(classOf[BrokerRouter], maxBrokerOfTopic, maxTopicInABroker), "brokerRouter")
    topicRouter = context.actorOf(Props[TopicRouter], "topicRouter")
    clientRouter = context.actorOf(Props[ClientRouter], "clientRouter")

    toBrokerServer = new ServerNIO(toBrokerPort, new ServerWorkerHandlerFactory() {
      def createServerWorkerHandler(connection: TransportConnection_Thread): ServerWorkerHandler = {
        new BrokerServerHandler(connection, brokerRouter, brokerRecheckTime)
      }
    })
    toBrokerServer.start
    log.info("broker-server启动完成")

    toClientServer = new ServerNIO(toClientPort, new ServerWorkerHandlerFactory() {
      def createServerWorkerHandler(connection: TransportConnection_Thread): ServerWorkerHandler = {
        log.debug("新建一个clientHandler")
        new ClientServerHandler(connection, clientRouter, clientRecheckTime)
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


