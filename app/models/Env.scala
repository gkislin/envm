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

  def vhost(hostName: String): VHost = env.servers.map(_.vhosts).flatten.find(_.name == hostName).get

  //  ; vhost<-server.vhosts; if vhost.name==hostName) yield vhost).head

  def serverHosts(serverName: String): Seq[ExtVHost] = extVHosts.filter(_.serverName == serverName)

  def parse(): Env = Json.parse(loadEnv).as[Env]

  def url(protocol: String, ip: String, port: port, path: String): String = s"$protocol://$ip:$port/$path"

  def vpnSsh(server: Server) = s"ssh ${server.userValue}@${server.vpnIp.get}"

  def ssh(server: Server) = s"ssh -p ${server.ssh} ${server.userValue}@${server.ip.get}"

  def ssh(host: VHost, isVpn: Boolean) = s"ssh -p ${host.sshPort} ${host.userValue}@${host.accessIp(isVpn)}"

  def psql(vhost: VHost, isVpn: Boolean) = s"jdbc:postgresql://${vhost.accessIp(isVpn)}:${vhost.psqlPort(isVpn)}/rdb"

  def psqlExt(vhost: VHost, isVpn: Boolean) = {
    val (user, passw) = getLoginPassw("psql", Some(vhost))
    s"jdbc:postgresql://${vhost.accessIp(isVpn)}:${vhost.psqlPort(isVpn)}/rdb?user=$user&password=$passw"
  }

  def reload() {
    env = parse()
    serversJson = JsonUtil.toJsonStr(env.servers)
    extVHosts =
      for (server <- env.servers; vhost <- server.vhosts)
      yield ExtVHost(vhost.name, vhost.descr, vhost.ip != None, vhost.vpnIp != None, getVersion(vhost.name), server.name)
  }
}


trait Host {
  val name: String
  val descr: String
  val user: Option[String]
  val ip: Option[String]
  val vpnIp: Option[String]

  lazy val userValue = user.getOrElse(Env.env.user)
}

case class Env(tomcat: Env.port,
               wso2: Env.port,
               wsdl: Env.port,
               psql: Env.port,
               ssh: Env.port,
               ctx: String,
               user: String,
               accessIp: String,
               servers: List[Server])

case class Server(name: String, descr: String, user: Option[String],
                  ssh: Env.port,
                  ip: Option[String],
                  vpnIp: Option[String],
                  vhosts: List[VHost]) extends Host {
}

case class VHost(name: String, descr: String,
                 tomcat: Option[Env.port],
                 wso2: Option[Env.port],
                 wsdl: Option[Env.port],
                 psql: Option[Env.port],
                 ssh: Option[Env.port],
                 ip: Option[String],
                 vpnIp: Option[String],
                 ctx: Option[String], user: Option[String]) extends Host {

  lazy val sshPort = ssh.getOrElse(Env.env.ssh)
  lazy val ctxValue = ctx.getOrElse(Env.env.ctx)

  def psqlPort(isVpn: Boolean) = if (isVpn) Env.env.psql else psql.getOrElse(Env.env.psql)

  def accessIp(isVpn: Boolean) = if (isVpn) vpnIp.get else Env.env.accessIp

  def tomcatPort(isVpn: Boolean) = if (isVpn) Env.env.tomcat else tomcat.getOrElse(Env.env.tomcat)

  def wsdlPort(isVpn: Boolean) = if (isVpn) Env.env.wsdl else wsdl.getOrElse(Env.env.wsdl)
}

case class ExtVHost(name: String, descr: String, isIp: Boolean, isVpn: Boolean,
                    var version: String, serverName: String)