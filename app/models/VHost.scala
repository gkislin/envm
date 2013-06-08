package models

import play.api.libs.json._


object EnvJson {

  implicit val envFormat: Format[VHost] = Json.format[VHost]

  var envs: List[VHost] = Nil
  var jsonValue: String = ""
  reload()

  def loadEnv: String = {
//    val source = scala.io.Source.fromFile("env.json", "UTF-8")
    val source = scala.io.Source.fromFile("envDummy.json", "UTF-8")
    val lines = source.getLines().mkString("\n")
    source.close()
    lines
  }

  def parse(json: String): List[VHost] = Json.parse(json).as[List[VHost]]

  def stringify(envs: List[VHost]): String = Json.stringify(Json.toJson(envs))

  def reload() {
    envs = parse(loadEnv)
    jsonValue = stringify(envs)
  }
}

object VHost {
  type port = Int

  val accessIp = "accessIp"

  def all: List[VHost] = EnvJson.envs
}

case class VHost(name: String, descr: String, version: Option[String],
               tomcat: VHost.port,
               wso2: Option[VHost.port], wsdl: Option[VHost.port],
               psql: VHost.port, ssh: VHost.port,
               ip: String, ctx: String, user: String) {

  def url(protocol: String, port: Option[VHost.port], path: String): String = port match {
    case Some(p) => url(protocol, p, path)
    case None => ""
  }

  def url(protocol: String, port: VHost.port, path: String): String = s"$protocol://${VHost.accessIp}:$port/$path"
}

