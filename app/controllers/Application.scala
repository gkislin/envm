package controllers

import _root_.db.Browser
import play.api._
import play.api.mvc._
import models.Env.extVhostFormat
import views._

import models._
import play.api.templates.Html
import util.{JsonUtil, Config}

object Application extends Controller {
  private val ALL = "All"

  def index = Action {
    Ok(html.index("Environment Manager"))
  }

  def serverDetail(serverName: String) = Action {
    Ok(
      if (serverName == ALL) Html("")
      else html.serverDetail(Env.server(serverName))
    )
  }

  def vhostDetail(serverName: String, name: String) = Action {
    Ok(
      html.vhostDetail(Env.vhost(serverName, name)))
  }

  def servers = Action {
    Ok(Env.serversJson).as(JSON)
  }

  def setHostVersion(name: String, version: String) = Action {
    Ok(Config.setVersion(name, version))
  }

  def browseDB(hostName: String) = Action {
    Ok(html.browseDb(hostName))
  }

  def vhosts(serverName: String) = Action {
    Ok(JsonUtil.toJsonStr(
      if (serverName == ALL) Env.extVHosts
      else Env.serverHosts(serverName)
    )).as(JSON)
  }

  def reload = Action {
    Env.reload()
    Ok(Env.serversJson).as(JSON)
  }

  def dbEntities(hostName: String, dbType: String, mask: String) = Action {
    Ok(Browser.entityList(Env.vhost(hostName), dbType, mask)).as(JSON)
  }

  def dbTypes(hostName: String, dbType: String) = Action {
    Ok(Browser.entityList(Env.vhost(hostName), dbType, "")).as(JSON)
  }

  def dbEntity(hostName: String, dbType: String, name: String) = Action {
    Ok(Browser.entityDescr(Env.vhost(hostName), dbType, name)).as(HTML)
  }

  def javascriptRoutes() = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          // Routers
          routes.javascript.Application.serverDetail,
          routes.javascript.Application.vhostDetail,
          routes.javascript.Application.servers,
          routes.javascript.Application.vhosts,
          routes.javascript.Application.dbEntities,
          routes.javascript.Application.dbEntity,
          routes.javascript.Application.dbTypes
        )
      ).as(JAVASCRIPT)
  }
}
