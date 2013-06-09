package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import models._
import Env._

import views._

import models._

object Application extends Controller {

  /**
   * Describes the hello form.
   */
  val vhostForm: Form[VHost] = Form(
    mapping(
      "name" -> nonEmptyText,
      "descr" -> nonEmptyText,
      "version" -> optional(text),
      "tomcat" -> number(min = 1, max = Int.MaxValue),
      "wso2" -> optional(number),
      "wsdl" -> optional(number),
      "psql" -> number(min = 1, max = Int.MaxValue),
      "ssh" -> number(min = 1, max = Int.MaxValue),
      "ip" -> text.verifying(
        pattern( """\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b""".r, error = "Wrong IP")),
      "ctx" -> nonEmptyText,
      "user" -> nonEmptyText
    )(VHost.apply)(VHost.unapply)
  )

  // -- Actions

  def index = Action {
    Ok(html.index("Environment Manager"))
  }

  def serverDetail(serverName: String) = Action {
    Ok(
      html.serverDetail(Env.server(serverName)))
  }

  def vhostDetail(serverName: String, name: String) = Action {
    Ok(
      html.vhostDetail(Env.vhost(serverName, name)))
  }

  def servers = Action {
    Ok(toJsonStr(Env.servers)).as(JSON)
  }

  def vhosts(serverName: String) = Action {
    Ok(toJsonStr(Env.vhosts(serverName))).as(JSON)
  }

  def reload = Action {
    Env.reload()
    Ok(toJsonStr(Env.servers)).as(JSON)
  }

  def javascriptRoutes() = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          // Routers
          routes.javascript.Application.serverDetail,
          routes.javascript.Application.vhostDetail,
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
