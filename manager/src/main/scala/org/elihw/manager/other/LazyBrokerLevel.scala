package org.elihw.manager.other

/**
 * User: bigbully
 * Date: 13-12-21
 * Time: 下午3:34
 */
object LazyBrokerLevel extends Enumeration {

  val LOW = 1
  val NORMAL = 2
  val HIGH = 3

  var LEVEL1 = 5000 * 60
  var LEVEL2 = 50000 * 60
  var LEVEL3 = 500000 * 60
}
