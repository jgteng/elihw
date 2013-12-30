package org.elihw.manager.function

import org.scalatest._
import akka.actor.{ActorIdentity, Identify, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.elihw.manager.Manager
import org.elihw.manager.mail.{StartManagerMail, CreateMail, RegisterBrokerMail}
import com.jd.bdp.whale.common.command.RegisterBrokerReqCmd
import org.scalamock.scalatest.proxy.MockFactory
import org.elihw.manager.communication.BrokerServerHandler
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.io.Directory


/**
 * User: bigbully
 * Date: 13-12-26
 * Time: 下午10:15
 */
class ManagerTest(mySystem: ActorSystem) extends TestKit(mySystem) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll with MockFactory {


  def this() = this(ActorSystem("manager"))
  implicit val timeout = Timeout(2 seconds)


  override protected def beforeAll(): Unit = {
    val baseDir = "/home/bigbully/program/workspace/scala/elihw/manager/src/test/resources"
    mySystem.actorOf(Props[Manager])  ! StartManagerMail(baseDir)
    Thread.sleep(1000)
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  behavior of "Broker"

  it should "register" in {
    val brokerRouter = mySystem.actorSelection("/user/manager/brokerRouter")
    val registerBrokerReqCmd = new RegisterBrokerReqCmd()
    registerBrokerReqCmd.setId(1)
    val handler = mock[BrokerServerHandler]
    brokerRouter ! RegisterBrokerMail(registerBrokerReqCmd, handler)
    val future = ask(mySystem.actorSelection("/user/manager/brokerRouter/1"), Identify(1)).mapTo[ActorIdentity]
    future.foreach {
      (actorIdentity: ActorIdentity) => {
        actorIdentity match {
          case ActorIdentity(brokerId, Some(ref)) => {
            (handler.finishRegister _).expects(ref)
            println(1)
          }
          case ActorIdentity(brokerId, None) => {
            println(2)
          }
        }
      }
    }
  }

}

