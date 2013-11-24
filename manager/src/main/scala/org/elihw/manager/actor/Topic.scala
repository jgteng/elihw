package org.elihw.manager.actor

import akka.actor.{ActorRef, Actor}
import org.elihw.manager.mail.CreateMail

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:02
 */
class Topic extends Actor {

  var brokerMap: Map[Int, ActorRef] = Map()

  def receive = {
    case creatMail: CreateMail => {
      brokerMap += (creatMail.brokerId -> creatMail.broker)
    }
  }
}
