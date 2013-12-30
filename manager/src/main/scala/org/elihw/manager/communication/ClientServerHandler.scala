package org.elihw.manager.communication

import com.jd.bdp.whale.communication.ServerWorkerHandler
import akka.actor.ActorRef
import org.elihw.manager.actor.Broker.BrokerInfo

/**
 * User: bigbully
 * Date: 13-12-28
 * Time: 下午7:27
 */
trait ClientServerHandler extends ServerWorkerHandler {

  def finishRegister(client: ActorRef, topicName: String, baseInfos: Set[BrokerInfo])
  def notifyBrokerOnLine(brokerId:String, topicName:String)
  def notifyBrokerOffLine(brokerId:String, topicName:String)
}
