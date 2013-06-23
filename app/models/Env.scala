package models

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.json.JsArray
import play.api.Play

import Config._
import java.io.{PrintWriter, InputStreamReader, FileInputStream}
import java.util.Properties
import resource._

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

  def serverHosts(serverName: String): Seq[ExtVHost] = extVHosts.filter(_.serverName == serverName)

  def parse(): Env = Json.parse(loadEnv).as[Env]

  def toJsonStr[T](iterable: Iterable[T])(implicit w: Writes[T]): String = Json.stringify(JsArray(iterable.map(toJson(_)).toSeq))

  def toJsonStr[T](o: T)(implicit w: Writes[T]): String = Json.stringify(toJson(o))

  def url(protocol: String, port: port, path: String): String = s"$protocol://${env.accessIp}:$port/$path"

  def ssh(host: Host, ip: String = env.accessIp) = s"ssh -p ${host.ssh} ${host.user}@$ip"

  def psql(host: VHost, ip: String = env.accessIp) = s"jdbc:postgresql://$ip:${host.psql}/rdb"

  def reload() {
    env = parse()
    serversJson = toJsonStr(env.servers)
    extVHosts =
      for (server <- env.servers; vhost <- server.vhosts)
      yield ExtVHost(vhost.name, vhost.descr, getVersion(vhost.name), server.name)
  }
}

object Config {
  private val versionFile = "version.conf"

  val props: Properties = managed(new InputStreamReader(new FileInputStream(versionFile), "UTF8")).acquireAndGet {
    val p = new Properties()
    in => p.load(in)
      p
  }

  def envConfigName() = getFromConf("env.config", "env.json")

  def getFromConf(name: String, default: String): String = getFromConf(name, "%s", default, None)

  def getFromConf(name: String, pattern: String, default: String): String = getFromConf(name, pattern, default, None)

  def getLoginPassw(name: String, host: Option[VHost]): (String, String) = {
    val str = getFromConf(s"$name.passw", "%s", "/", host)
    str.split('/') match {
      case Array() => ("", "")
      case Array(val1) => (val1, "")
      case Array(val1, val2) => (val1, val2)
    }
    //    (if (array.length > 0) array(0) else "", if (array.length > 1) array(1) else "")
  }

  def getFromConf(name: String, pattern: String, default: String, opHost: Option[VHost]): String = {
    val list: List[String] = opHost match {
      case Some(vhost) => List(s"${vhost.name}.$name", name)
      case None => List(name)
    }
    list.find(!Play.current.configuration.getString(_).isEmpty) match {
      case Some(key) => pattern.format(Play.current.configuration.getString(key).get)
      case None => default
    }
  }


  def getVersion(name: String): String = props.getProperty(name + ".version", "")

  def setVersion(name: String, version: String): String = {
    val host: ExtVHost = Env.extVHosts.find(_.name == name).get
    if (host.version != version) {
      host.version = version
      props.setProperty(name + ".version", version)
      val comment = s"Set $name.version=$version"
      managed(new PrintWriter(versionFile, "UTF-8")).acquireAndGet {
        out => props.store(out, comment)
      }
      comment
    } else {
      "Version unchanged"
    }
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