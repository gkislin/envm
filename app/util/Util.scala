package util

import java.util.Properties
import resource._
import java.io.{PrintWriter, FileInputStream, InputStreamReader}
import play.api.Play
import models.Env
import models.ExtVHost
import models.VHost
import scala.Some
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.json.JsArray

/**
 * User: gkislin
 * Date: 26.06.13
 */
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



object JsonUtil {
  def toJsonStr[T](iterable: Iterable[T])(implicit w: Writes[T]): String = Json.stringify(JsArray(iterable.map(toJson(_)).toSeq))

  def toJsonStr[T](o: T)(implicit w: Writes[T]): String = Json.stringify(toJson(o))
}
