package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail.{Mail, BrokerOfTopicResMail, BrokerOfTopicReqMail, CreateMail}
import akka.pattern._
import scala.concurrent.Future
import org.elihw.manager.mail.CreateMail
import org.elihw.manager.mail.BrokerOfTopicResMail
import org.elihw.manager.mail.BrokerOfTopicReqMail
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:02
 */
class Topic extends Actor with ActorLogging{

  import context._

  implicit val timeout = Timeout(1 seconds)

  var brokerMap: Map[String, ActorRef] = Map()
  var clientMap: Map[String, ActorRef] = Map()

  def confirmAndUpdateStatus(id:String, from:String) ={
    val future: Future[ActorIdentity] = ask(actorSelection("/user/manager/" + from + "Router/" + id), Identify(id)).mapTo[ActorIdentity]
    future.foreach {
      (actorIdentity: ActorIdentity) => {
        actorIdentity match {
          case ActorIdentity(id, Some(ref)) => {
            from match {
              case Mail.BROKER => brokerMap += (id.toString -> ref)
              case Mail.CLIENT => clientMap += (id.toString -> ref)
            }
          }
          case ActorIdentity(id, None) => {
            log.info("当前{}{}已经被销毁,忽略", from, id)
          }
        }
      }
    }
  }

  def receive = {
    case createMail: CreateMail => {
      confirmAndUpdateStatus(createMail.id, createMail.from)
    }
    case brokerOfTopicReqMail: BrokerOfTopicReqMail => {
      sender ! BrokerOfTopicResMail(brokerMap)
    }
  }
}
