package org.elihw.manager.actor

import akka.actor.{Status, ActorRef, Actor}
import org.elihw.manager.mail.{FinishMail, CreateMail}

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:02
 */
class Topic extends Actor {

  var brokerMap: Map[Int, ActorRef] = Map()

  def receive = {
    case createMail: CreateMail => {
      val broker = createMail.broker
      brokerMap += (createMail.brokerId -> broker)
      broker ! FinishMail(self.path.name, self)
    }
  }
}
