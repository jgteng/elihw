package org.elihw.manager

import akka.actor._
import org.ini4j.{Profile, Ini}
import java.io.File
import com.jd.bdp.whale.communication.{ServerWorkerHandler, TransportConnection_Thread, ServerWorkerHandlerFactory, ServerNIO}
import org.elihw.manager.communication.{ClientServerHandler, BrokerServerHandler}
import org.elihw.manager.actor.{ClientRouter, TopicRouter, BrokerRouter}
import org.elihw.manager.mail.StartManagerMail


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

  var brokerRouter: ActorRef = null
  var topicRouter: ActorRef = null
  var clientRouter: ActorRef = null

  var toBrokerServer: ServerNIO = null
  var toClientServer: ServerNIO = null

  def initManagerServer(baseDir: String) = {
    val file = new File(baseDir + "/manager.ini")
    val ini = new Ini(file)
    val sec: Profile.Section = ini.get("port")
    val toClientPort: Int = Integer.parseInt(sec.get("client_port"))
    val toBrokerPort: Int = Integer.parseInt(sec.get("broker_port"))

    toBrokerServer = new ServerNIO(toBrokerPort, new ServerWorkerHandlerFactory() {
      def createServerWorkerHandler(connection: TransportConnection_Thread): ServerWorkerHandler = {
        new BrokerServerHandler(connection, brokerRouter)
      }
    })
    toBrokerServer.start
    log.info("broker-server启动完成")

    toClientServer = new ServerNIO(toClientPort, new ServerWorkerHandlerFactory() {
      def createServerWorkerHandler(connection: TransportConnection_Thread): ServerWorkerHandler = {
        new ClientServerHandler(connection, clientRouter)
      }
    })
    toBrokerServer.start
    log.info("broker-server启动完成")
  }

  def receive: Actor.Receive = {
    case startMail: StartManagerMail => {
      initManagerServer(startMail.baseDir)
    }
  }

  override def preStart(): Unit = {
    brokerRouter = context.actorOf(Props[BrokerRouter], "brokerRouter")
    topicRouter = context.actorOf(Props[TopicRouter], "topicRouter")
    clientRouter = context.actorOf(Props[ClientRouter], "clientRouter")
  }
}
