package org.elihw.manager.actor

import akka.actor.{Props, Actor}
import org.elihw.manager.mail.PublishMail

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:04
 */
class ClientRouter extends Actor{
  def receive = {
    case publishMail:PublishMail => {
      val client = context.actorOf(Props[Client], publishMail.cmd.getClientId)
      client ! publishMail
    }
  }
}
