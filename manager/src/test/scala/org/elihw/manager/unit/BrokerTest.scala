package org.elihw.manager.unit

import org.scalatest.{Matchers, FlatSpec}
import akka.testkit.TestActorRef
import org.elihw.manager.actor.Broker
import org.elihw.manager.other.LazyBrokerLevel
import akka.actor.{Props, ActorSystem}
import com.jd.bdp.whale.common.command.TopicHeartInfo
import java.util.concurrent.atomic.AtomicLong

/**
 * User: bigbully
 * Date: 13-12-25
 * Time: 下午9:00
 */
class BrokerTest extends FlatSpec with Matchers{

  import LazyBrokerLevel._
  implicit val system = ActorSystem("manager")

  behavior of "Broker Actor"

  it should "give HIGH lazy level when topicHeartInfos is null" in {
    val brokerRef = TestActorRef (new Broker(null, null))
    val broker = brokerRef.underlyingActor
    broker.topicHeartInfos = null
    broker.lazyLevel shouldBe HIGH
  }

  it should "give LOW lazy level when topicHeartInfos's score is less than LEVEL1" in {
    val brokerRef = TestActorRef (new Broker(null, null))
    val broker = brokerRef.underlyingActor
    val topicHeartInfo = new TopicHeartInfo
    topicHeartInfo.setProduceSum(new AtomicLong((LEVEL1 - 100)/ 2))
    topicHeartInfo.setConsumeSum(new AtomicLong((LEVEL1 - 100)/ 2))
    broker.topicHeartInfos = List(topicHeartInfo)
    broker.lazyLevel shouldBe LOW
  }

  it should "give NORMAL lazy level when topicHeartInfos's score is more than LEVEL1, less than LEVEL2" in {
    val brokerRef = TestActorRef (new Broker(null, null))
    val broker = brokerRef.underlyingActor
    val topicHeartInfo = new TopicHeartInfo
    topicHeartInfo.setProduceSum(new AtomicLong((LEVEL2 - LEVEL1 + 100)/ 2))
    topicHeartInfo.setConsumeSum(new AtomicLong((LEVEL2 - LEVEL1 + 100)/ 2))
    broker.topicHeartInfos = List(topicHeartInfo)
    broker.lazyLevel shouldBe NORMAL
  }

  it should "give HIGH lazy level when topicHeartInfos's score is more than LEVEL2" in {
    val brokerRef = TestActorRef (new Broker(null, null))
    val broker = brokerRef.underlyingActor
    val topicHeartInfo = new TopicHeartInfo
    topicHeartInfo.setProduceSum(new AtomicLong(LEVEL3/2))
    topicHeartInfo.setConsumeSum(new AtomicLong(LEVEL3/2))
    broker.topicHeartInfos = List(topicHeartInfo)
    broker.lazyLevel shouldBe HIGH
  }

}
