package org.elihw.manager.actor

import akka.actor.{Props, Actor}
import org.elihw.manager.mail.{StatusResMail, Mail, StatusMail, RegisterBroekrMail}
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import org.elihw.manager.actor.Broker.BrokerInfo

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 下午5:21
 */
class BrokerRouter extends Actor {

  import context._
  implicit val timeout = Timeout(1 seconds)

  def receive = {
    case registerMail: RegisterBroekrMail => {
      val broker = context.actorOf(Props(classOf[Broker], registerMail.handler, BrokerInfo(registerMail.cmd.getId, registerMail.cmd.getIp, registerMail.cmd.getPort)), registerMail.cmd.getId.toString)
      broker ! registerMail
    }
    case statusMail: StatusMail => {
      var list: List[BrokerInfo] = List[BrokerInfo]()
      children.foreach {
        child => {
          list +:= Await.result((child ? StatusMail(Mail.BROKER)), timeout.duration).asInstanceOf[BrokerInfo]
        }
      }
      sender ! StatusResMail(Mail.BROKER, list)
    }
  }
}
