package org.elihw.manager


import org.scalatest.{Matchers, FlatSpec, WordSpec, BeforeAndAfterAll}
import akka.testkit.TestActorRef
import org.elihw.manager.actor.Broker
import akka.actor.ActorSystem


/**
 * User: bigbully
 * Date: 13-11-2
 * Time: 下午11:28
 */
class ManagerTest extends FlatSpec with Matchers{
  implicit val system = ActorSystem("manager")

  behavior of "123"

  it should "123" in {
    val broker = TestActorRef[Broker]
    broker ! "123123123"
  }
}
