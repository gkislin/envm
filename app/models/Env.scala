package models

import util.Config._
import resource._
import play.api.libs.json.{Json, Format}
import util.JsonUtil

object Env {
  type port = Int
  implicit val vhostFormat: Format[VHost] = Json.format[VHost]
  implicit val extVhostFormat: Format[ExtVHost] = Json.format[ExtVHost]
  implicit val serverFormat: Format[Server] = Json.format[Server]
  implicit val envFormat: Format[Env] = Json.format[Env]

  var env: Env = _
  var serversJson: String = _
  var extVHosts: List[ExtVHost] = _
  reload()

  def loadEnv: String =
    managed(scala.io.Source.fromFile(envConfigName, "UTF-8")).acquireAndGet {
      source => source.getLines().mkString("\n")
    }

  def server(serverName: String): Server = env.servers.find(_.name == serverName).get

  def vhost(serverName: String, name: String): VHost = server(serverName).vhosts.find(_.name == name).get

  def vhost(hostName: String): VHost = env.servers.map(_.vhosts).flatten.find(_.name==hostName).get
//  ; vhost<-server.vhosts; if vhost.name==hostName) yield vhost).head

  def serverHosts(serverName: String): Seq[ExtVHost] = extVHosts.filter(_.serverName == serverName)

  def parse(): Env = Json.parse(loadEnv).as[Env]

  def url(protocol: String, port: port, path: String): String = s"$protocol://${env.accessIp}:$port/$path"

  def ssh(host: Host, ip: String = env.accessIp) = s"ssh -p ${host.ssh} ${host.user}@$ip"

  def psql(vhost: VHost, ip: String = env.accessIp) = s"jdbc:postgresql://$ip:${vhost.psql}/rdb"

  def psqlExt(vhost: VHost, ip: String = env.accessIp) = {
    val (user, passw) = getLoginPassw("psql", Some(vhost))
    s"jdbc:postgresql://$ip:${vhost.psql}/rdb?user=$user&password=$passw"
  }

  def reload() {
    env = parse()
    serversJson = JsonUtil.toJsonStr(env.servers)
    extVHosts =
      for (server <- env.servers; vhost <- server.vhosts)
      yield ExtVHost(vhost.name, vhost.descr, getVersion(vhost.name), server.name)
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

case class VHost(name: String, descr: String,
                 tomcat: Env.port,
                 wso2: Option[Env.port], wsdl: Option[Env.port],
                 psql: Env.port, ssh: Env.port,
                 ip: String, ctx: String, user: String) extends Host

case class ExtVHost(name: String, descr: String,
                    var version: String, serverName: String)