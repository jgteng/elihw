package org.elihw.manager.actor

import org.elihw.manager.communication.ClientServerHandler
import org.elihw.manager.actor.Client.ClientInfo
import org.elihw.manager.mail.Mail

/**
 * User: bigbully
 * Date: 13-11-30
 * Time: 下午10:05
 */
class Consumer(handler:ClientServerHandler, val group:String) extends Client(handler){

  override def getInfo: ClientInfo = {
    ClientInfo(self.path.name, Mail.CONSUMER, group, topic, brokers)
  }
}
