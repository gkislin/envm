package models

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.json.JsArray
import play.api.Play

object Env {
  type port = Int
  implicit val vhostFormat: Format[VHost] = Json.format[VHost]
  implicit val serverFormat: Format[Server] = Json.format[Server]
  implicit val envFormat: Format[Env] = Json.format[Env]

  var env: Env = parse()
  var jsonValue: String = toJsonStr(env)

  def loadEnv: String = {
    //    val source = scala.io.Source.fromFile("env.json", "UTF-8")
    val source = scala.io.Source.fromFile(envConfigName, "UTF-8")
    val lines = source.getLines().mkString("\n")
    source.close()
    lines
  }

  def envConfigName(): String = Play.current.configuration.getString("env.config").getOrElse("env.json")

  def servers(): List[Server] = env.servers

  def server(serverName: String): Server = servers().filter(_.name == serverName)(0)

  def vhosts(serverName: String): List[VHost] = server(serverName).vhosts

  def vhost(serverName: String, name: String): VHost = vhosts(serverName).filter(_.name == name)(0)

  def parse(): Env = Json.parse(loadEnv).as[Env]

  def toJsonStr[T](iterable: Iterable[T])(implicit w: Writes[T]): String = Json.stringify(JsArray(iterable.map(toJson(_)).toSeq))

  def toJsonStr[T](o: T)(implicit w: Writes[T]): String = Json.stringify(toJson(o))

  def url(protocol: String, port: port, path: String): String = s"$protocol://${env.accessIp}:$port/$path"

  def ssh(host: Host, ip: String= env.accessIp) = s"ssh -p ${host.ssh} ${host.user}@$ip"

  def reload() {
    env = parse()
    jsonValue = toJsonStr(env)
  }
}

trait Host {
  val name: String
  val descr: String
  val ssh: Env.port
  val ip: String
  val user: String
}

case class Env(accessIp: String, servers: List[Server])

case class Server(name: String, descr: String, ssh: Env.port, ip: String, user: String,
                  vhosts: List[VHost]) extends Host

case class VHost(name: String, descr: String, version: Option[String],
                 tomcat: Env.port,
                 wso2: Option[Env.port], wsdl: Option[Env.port],
                 psql: Env.port, ssh: Env.port,
                 ip: String, ctx: String, user: String = "user") extends Host
