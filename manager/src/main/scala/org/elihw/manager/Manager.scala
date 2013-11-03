package org.elihw.manager

import akka.actor.{Props, ActorSystem}


/**
 * User: bigbully
 * Date: 13-10-29
 * Time: 下午10:11
 */
object Manager {

  def main(args:Array[String]){
    println(123)
    val system = ActorSystem("elihw-system")
//    val greeter = system.actorOf(Props[BrokerListener], "greeter")
  }

}
