package db

import models.VHost
import anorm._
import play.api.libs.json.{Json, Writes}
import util.JsonUtil
import anorm.SqlParser.scalar

/**
 * User: gkislin
 * Date: 26.06.13
 */

case class Entity(name: String, comment: Option[String], dbtype: String)

//case class Entity(name: String, comment: String, dbtype: String)

object Browser {
  implicit val entityFormat: Writes[Entity] = Json.writes[Entity]

  def entityList(vhost: VHost, dbType: String, mask: String): String =
    JsonUtil.toJsonStr(Db.withConnection(vhost) {
      implicit c =>
        SQL("SELECT * FROM type_list({dbType}, {mask}) AS (name TEXT, comment TEXT, dbtype TEXT)")
          .on("dbType" -> dbType, "mask" -> mask)().map {
          row => Entity(row[String]("name"), row[Option[String]]("comment"), row[String]("dbtype"))
        }
    })

  def entityDescr(vhost: VHost, dbType: String, name: String): String =
    Db.withConnection(vhost) {
      implicit c =>
        SQL("SELECT * FROM entity_html({dbType}, {name})")
          .on("dbType" -> dbType, "name" -> name).as(scalar[String].single)
    }
}
