package org.elihw.manager.actor

import akka.actor._
import org.elihw.manager.mail._
import akka.pattern._
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import org.elihw.manager.mail.CreateMail
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import org.elihw.manager.other.Info
import org.elihw.manager.actor.Topic.TopicInfo

/**
 * User: bigbully
 * Date: 13-11-5
 * Time: 下午11:02
 */
class Topic extends Actor with ActorLogging {

  import context._

  implicit val timeout = Timeout(1 seconds)

  var brokers: Set[ActorPath] = Set()
  var clients: Set[ActorPath] = Set()

  def confirmAndUpdateStatus(id: String, from: String) = {
    val future: Future[ActorIdentity] = ask(actorSelection("/user/manager/" + from + "Router/" + id), Identify(id)).mapTo[ActorIdentity]
    future.foreach {
      (actorIdentity: ActorIdentity) => {
        actorIdentity match {
          case ActorIdentity(id, Some(ref)) => {
            from match {
              case Mail.BROKER => {
                log.debug("topic:{}关联broker:{}", self.path.name, id)
                brokers += ref.path
                ref ! FinishMail(self.path)
              }
              case Mail.CLIENT => {
                log.debug("topic:{}关联client:{}", self.path.name, id)
                clients += ref.path
                ref ! FinishMail(self.path, brokers)
              }
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
    case statusMail: StatusMail => {
      sender ! TopicInfo(self.path.name, brokers, clients)
    }
  }
}

object Topic {
  case class TopicInfo(val name:String, val brokers: Set[ActorPath], val clients: Set[ActorPath]) extends Info{}
}
