package org.elihw.manager.Util

/**
 * User: bigbully
 * Date: 13-12-3
 * Time: 上午12:14
 */
class Model

object Model {
  def broker:Model = {
    new Broker1()
  }
  def client:Model = {
    new Broker1()
  }
}

class Broker1 extends Model
class Broker extends Model
class Client extends Model

