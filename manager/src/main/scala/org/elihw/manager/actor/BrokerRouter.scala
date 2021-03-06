package org.elihw.manager.actor

import akka.actor.{ActorLogging, ActorPath, Props, Actor}
import org.elihw.manager.mail._
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import org.elihw.manager.actor.Broker.BrokerInfo
import org.elihw.manager.other.{BrokerScale, LazyBrokerLevel}
import org.elihw.manager.mail.StatusResMail
import org.elihw.manager.mail.FindLazyBrokersMail
import org.elihw.manager.mail.RegisterBrokerMail

/**
 * User: biandi
 * Date: 13-11-22
 * Time: 下午5:21
 */
class BrokerRouter extends Actor with ActorLogging {

  import Mail._
  import context._
  import BrokerScale._

  implicit val timeout = Timeout(1 seconds)


  //循环获取最懒惰的broker列表，如果没有最懒惰的，就取次懒惰的，以此类推
  def findLazyBrokers: Set[ActorPath] = {
    import LazyBrokerLevel._

    var lazyBrokers = Set[ActorPath]()
    var setInBroker = 0
    var lazyLevel = LOW

    while (setInBroker < MAX_BROKER_OF_TOPIC && lazyLevel <= HIGH && setInBroker < children.size) {
      val lazyBrokerMap: Map[Int, Set[BrokerInfo]] = getLazyBrokerMap
      val lazyBrokerInfos = lazyBrokerMap.getOrElse(lazyLevel, Set[BrokerInfo]())

      for (brokerInfo <- lazyBrokerInfos) {
        if (brokerInfo.topics.size < MAX_TOPIC_IN_A_BROKER) {
          lazyBrokers += child(brokerInfo.id.toString).get.path
          setInBroker += 1
        }
      }
      lazyLevel += 1
    }

    lazyBrokers
  }


  //计算当前所有broker的懒惰程度，按等级返回
  def getLazyBrokerMap: Map[Int, Set[BrokerInfo]] = {
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
    case registerMail: RegisterBrokerMail => {
      val broker = child(registerMail.cmd.getId.toString).getOrElse(
        actorOf(Props(classOf[Broker], registerMail.handler, BrokerInfo(registerMail.cmd.getId, registerMail.cmd.getIp, registerMail.cmd.getPort))
          , registerMail.cmd.getId.toString))
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
      val lazyBrokers = findLazyBrokers
      //todo 先在broker上创建,然后client再通知,如何解决先后问题
      for(brokerPath <- lazyBrokers){
        actorSelection(brokerPath) ! CreateTopicMail(findLazyBrokersMail.topic)
      }
      for (clientPath <- findLazyBrokersMail.clients){
        actorSelection(findLazyBrokersMail.topic) ! FindLazyBrokersResMail(lazyBrokers, clientPath)
      }
    }
  }

}
