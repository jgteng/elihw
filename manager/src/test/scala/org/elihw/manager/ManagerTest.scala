package org.elihw.manager


import org.scalatest.{Matchers, FlatSpec}
import akka.testkit.TestActorRef
import org.elihw.manager.actor.Broker
import akka.actor.ActorSystem

/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午11:28
 */

class ManagerTest extends FlatSpec with Matchers {
  implicit val system = ActorSystem("manager")

  //  behavior of "TopicSet"
  //
  //  it should "create different topic actor" in {
  //    val topicSet = system.actorOf(Props[TopicSet], "topics")
  //    val brokerRegister = new BrokerRegister(1,  "127.0.0.1", 8080, MASTER, "myCluster", List("topic1", "topic2"))
  //    topicSet ! brokerRegister
  //    val topic1 = system.actorSelection("topics/topic1")
  //    val topic2 = system.actorSelection("topics/topic2")
  //    topic1 shouldNot be (null)
  //    topic2 shouldNot be (null)
  //  }

  behavior of "broker"

  it should "receive String" in {
    val broker = TestActorRef[Broker]
    broker.receive("123123")
  }
}
