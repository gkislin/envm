package models

import play.api.libs.json._


object EnvJson {

  implicit val envFormat: Format[Env] = Json.format[Env]

  var envs: List[Env] = Nil
  var jsonValue: String = ""
  reload()

  def loadEnv: String = {
//    val source = scala.io.Source.fromFile("env.json", "UTF-8")
    val source = scala.io.Source.fromFile("envDummy.json", "UTF-8")
    val lines = source.getLines().mkString("\n")
    source.close()
    lines
  }

  def parse(json: String): List[Env] = Json.parse(json).as[List[Env]]

  def stringify(envs: List[Env]): String = Json.stringify(Json.toJson(envs))

  def reload() {
    envs = parse(loadEnv)
    jsonValue = stringify(envs)
  }
}

object Env {
  type port = Int

  val accessIp = "accessIp"

  def all: List[Env] = EnvJson.envs
}

case class Env(name: String, descr: String, version: Option[String],
               tomcat: Env.port,
               wso2: Option[Env.port], wsdl: Option[Env.port],
               psql: Env.port, ssh: Env.port,
               ip: String, ctx: String, user: String) {

  def url(protocol: String, port: Option[Env.port], path: String): String = port match {
    case Some(p) => url(protocol, p, path)
    case None => ""
  }

  def url(protocol: String, port: Env.port, path: String): String = s"$protocol://${Env.accessIp}:$port/$path"
}

