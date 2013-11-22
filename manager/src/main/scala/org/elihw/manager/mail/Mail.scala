package org.elihw.manager.mail

import org.elihw.manager.communication.BrokerServerHandler

/**
 * User: bigbully
 * Date: 13-10-29
 * Time: 下午10:31
 */
sealed trait Mail{


}
object MailEnum extends Enumeration{
  val MASTER = 0;
  val SLAVE = 1;
}

case class BrokerRegisterMail(val brokerServerHandler:BrokerServerHandler) extends Mail {

}

case class StartManagerMail(val baseDir:String) extends Mail {
  override def toString:String = "baseDir:" + baseDir
}



