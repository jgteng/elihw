package org.elihw.manager.communication

import com.jd.bdp.whale.communication.ServerWorkerHandler
import com.jd.bdp.whale.communication.message.Message

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 上午10:33
 */
class BrokerServerHandler extends ServerWorkerHandler{

  def transportOnException(p1: Exception) = {

  }

  def doMsgHandler(p1: Message): Message = ???
}
