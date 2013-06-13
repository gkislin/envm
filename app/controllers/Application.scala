package controllers

import play.api._
import play.api.mvc._
import models._
import Env._

import views._

import models._
import play.api.templates.Html

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

  def vhosts(serverName: String) = Action {
    Ok(toJsonStr(
      if (serverName == ALL) Env.extVHosts
      else Env.serverHosts(serverName)
    )).as(JSON)
  }

  def reload = Action {
    Env.reload()
    Ok(Env.serversJson).as(JSON)
  }

  def javascriptRoutes() = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          // Routers
          routes.javascript.Application.serverDetail,
          routes.javascript.Application.vhostDetail,
          routes.javascript.Application.servers,
          routes.javascript.Application.vhosts
        )
      ).as(JAVASCRIPT)
  }

  /**
   * Handles the form submission.
  def sayHello = Action { implicit request =>
    helloForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.index(formWithErrors)),
      {case (name, repeat, color) => Ok(html.hello(name, repeat.toInt, color))}
    )
  }
   */

}
