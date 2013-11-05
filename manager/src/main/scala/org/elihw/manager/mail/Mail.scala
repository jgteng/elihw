package org.elihw.manager.mail

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

class BrokerRegister(id:Int, ip:String, port:Int, isMaster:Int, cluster:String, topicNames:List[String]) extends Mail {

}

