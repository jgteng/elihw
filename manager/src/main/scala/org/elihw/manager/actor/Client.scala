package org.elihw.manager.actor

import akka.actor.Actor
import org.elihw.manager.mail.PublishMail
import org.elihw.manager.communication.ClientServerHandler

/**
 * User: bigbully
 * Date: 13-11-27
 * Time: 下午9:04
 */
class Client extends Actor{

  var handler:ClientServerHandler = null

  def receive = {
    case publishMail:PublishMail => {
      handler = publishMail.handler

    }
  }
}
