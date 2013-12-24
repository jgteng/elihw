package org.elihw.manager.actor

import akka.actor.{ActorLogging, ActorPath, Props, Actor}
import org.elihw.manager.mail._
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.other.LazyBrokerLevel
import org.elihw.manager.mail.StatusResMail
import org.elihw.manager.mail.FindLazyBrokersMail
import org.elihw.manager.mail.RegisterBroekrMail

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 下午5:21
 */
class BrokerRouter(val maxBrokerOfTopic: Int, val maxTopicInABroker: Int) extends Actor with ActorLogging {

  import Mail._
  import context._

  implicit val timeout = Timeout(1 seconds)

  private def findLazyBrokers: Set[ActorPath] = {
    import LazyBrokerLevel._

    var lazyBrokers = Set[ActorPath]()
    var setInBroker = 0
    var lazyLevel = LOW

    while (setInBroker < maxBrokerOfTopic && lazyLevel <= HIGH && setInBroker < children.size) {
      val lazyBrokerMap: Map[Int, Set[BrokerInfo]] = getLazyBrokerMap
      val lazyBrokerInfos = lazyBrokerMap.getOrElse(lazyLevel, Set[BrokerInfo]())

      for (brokerInfo <- lazyBrokerInfos) {
        if (brokerInfo.topics.size < maxTopicInABroker) {
          lazyBrokers += child(brokerInfo.id.toString).get.path
          setInBroker += 1
        }
      }
      lazyLevel += 1
    }

    lazyBrokers
  }

  private def getLazyBrokerMap: Map[Int, Set[BrokerInfo]] = {
    var lazyBrokerHolderMap: Map[Int, Set[BrokerInfo]] = Map[Int, Set[BrokerInfo]]()
    for (broker <- children) {
      if (Await.result((broker ? IsConnectedMail), timeout.duration).asInstanceOf[Boolean]) {
        val brokerInfo = Await.result((broker ? BrokerStatusMail(true)), timeout.duration).asInstanceOf[BrokerInfo]
        var set = lazyBrokerHolderMap.getOrElse(brokerInfo.lazyLevl, Set[BrokerInfo]())
        set += brokerInfo
        lazyBrokerHolderMap += (brokerInfo.lazyLevl -> set)
      }
    }
    lazyBrokerHolderMap
  }

  def receive = {
    case registerMail: RegisterBroekrMail => {
      val broker = context.actorOf(Props(classOf[Broker], registerMail.handler, BrokerInfo(registerMail.cmd.getId, registerMail.cmd.getIp, registerMail.cmd.getPort)), registerMail.cmd.getId.toString)
      broker ! registerMail
    }
    case StatusMail => {
      var list: List[BrokerInfo] = List[BrokerInfo]()
      children.foreach {
        child => {
          list +:= Await.result((child ? StatusMail), timeout.duration).asInstanceOf[BrokerInfo]
        }
      }
      sender ! StatusResMail(Mail.BROKER, list)
    }
    case findLazyBrokersMail: FindLazyBrokersMail => {
      actorSelection(findLazyBrokersMail.topic) ! FindLazyBrokersResMail(findLazyBrokers, findLazyBrokersMail.client)
    }
  }

}
