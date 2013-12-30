package org.elihw.manager.communication

import com.jd.bdp.whale.communication.ServerWorkerHandler
import akka.actor.ActorRef

/**
 * User: bigbully
 * Date: 13-12-28
 * Time: 下午4:29
 */
trait BrokerServerHandler extends ServerWorkerHandler{

  def finishRegister(brokerPath: ActorRef)
  def createTopic(topicName:String):Boolean

}
