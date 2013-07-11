package models


import util.Config

case class User(login: String, password: String)

object User {

  /**
   * Authenticate a User.
   */
  def authenticate(login: String, password: String): Boolean = {
    if (Config.get("login", "") == login && Config.get("password", "") == password) true
    else false
  }
}
