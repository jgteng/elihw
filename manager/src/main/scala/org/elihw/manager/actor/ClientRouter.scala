package org.elihw.manager.actor

import akka.actor.{Props, Actor}
import org.elihw.manager.mail._
import com.jd.bdp.whale.common.communication.MessageType
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import org.elihw.manager.communication.WhaleClientHandler
import scala.concurrent.Await
import org.elihw.manager.mail.RegisterClientMail
import org.elihw.manager.mail.PublishMail
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import org.elihw.manager.actor.Client.ClientInfo

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:04
 */
class ClientRouter extends Actor {

  import Mail._
  import context._

  implicit val timeout = Timeout(1 seconds)

  def receive = {
    case publishMail: PublishMail => {
      register(publishMail.cmd.getClientId, publishMail.cmd.getClientType, publishMail.handler, publishMail)
    }
    case registerMail: RegisterClientMail => {
      register(registerMail.cmd.getClientId, registerMail.cmd.getClientType, registerMail.handler, registerMail)
    }
    case StatusMail => {
      var list: List[ClientInfo] = List[ClientInfo]()
      children.foreach {
        child => {
          list +:= Await.result((child ? StatusMail), timeout.duration).asInstanceOf[ClientInfo]
        }
      }
      sender ! StatusResMail(Mail.CLIENT, list)
    }
  }

  def register(clientId: String, clientType: Int, handler: WhaleClientHandler, mail: ClientMail) = {
    (actorSelection(clientId) ? Identify(clientId)).mapTo[ActorIdentity].foreach {
      (actorIdentity: ActorIdentity) => {
        actorIdentity match {
          case ActorIdentity(topicName, Some(ref)) => {
            ref ! mail
          }
          case ActorIdentity(topicName, None) => {
            clientType match {
              case MessageType.PRODUCER => {
                val client = actorOf(Props(classOf[Producer], handler), clientId)
                client ! mail
              }
              case MessageType.CONSUMERR => {
                val client = actorOf(Props(classOf[Consumer], handler, mail.asInstanceOf[PublishMail].cmd.getParamMap.get("group")), clientId)
                client ! mail
              }
            }
          }
        }
      }
    }
  }
}
