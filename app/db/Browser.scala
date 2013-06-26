package db

import models.VHost
import anorm._
import play.api.libs.json.{Json, Writes}
import util.JsonUtil

/**
 * User: gkislin
 * Date: 26.06.13
 */

case class Entity(name: String, comment: String, dbtype: String) {

}

object Browser {
  implicit val entityFormat: Writes[Entity] = Json.writes[Entity]

  def entityList(vhost: VHost, dbType: String, mask: String): String =
    JsonUtil.toJsonStr(Db.withConnection(vhost) {
      implicit c =>
        SQL("SELECT * FROM type_list({dbType}, {mask}) AS (name TEXT, comment TEXT, dbtype TEXT)")
          .on("dbType" -> dbType, "mask" -> mask)().map {
          case Row(name: String, comment: String, dbtype: String) => Entity(name, comment, dbtype)
        }
    })
}
