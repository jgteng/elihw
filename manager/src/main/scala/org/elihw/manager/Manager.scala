package org.elihw.manager

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.routing.{BroadcastRouter, FromConfig}


/**
 * User: bigbully
 * Date: 13-10-29
 * Time: 下午10:11
 */
object Manager {

  def main(args:Array[String]){
    val system = ActorSystem("manager")
    val manager = system.actorOf(Props[Manager], "manager")
    manager ! "start"
  }

}

class Manager extends Actor {

  def receive: Actor.Receive = {
    case "start" => {
      println("start")
    }
  }

  override def preStart(): Unit = {
    try {
      val topicRouter = context.actorOf(Props.empty.withRouter(FromConfig()), "topic-router")
      val brokerRouter = context.actorOf(Props.empty.withRouter(FromConfig()), "broker-router")
      val clientRouter = context.actorOf(Props.empty.withRouter(FromConfig()), "client-router")
    }catch {
      case e:Exception => e.printStackTrace
    }
  }
}
