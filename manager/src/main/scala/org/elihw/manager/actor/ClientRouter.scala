package org.elihw.manager.actor

import akka.actor.{Props, Actor}
import org.elihw.manager.mail.PublishMail
import org.elihw.manager.mail.Mail._

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:04
 */
class ClientRouter extends Actor{

  import context._

  def receive = {
    case publishMail:PublishMail => {
      publishMail.cmd.getClientType match {
        case 1 => {
          val client = actorOf(Props(classOf[Producer], publishMail.handler), publishMail.cmd.getClientId)
          client ! publishMail
        }
        case 0 => {
          val client = actorOf(Props(classOf[Consumer], publishMail.handler), publishMail.cmd.getClientId)
          client ! publishMail
        }
      }
    }
  }
}
