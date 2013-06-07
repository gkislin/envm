package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import views._

import models._

object Application extends Controller {

  /**
   * Describes the hello form.
   */
  val envForm: Form[Env] = Form(
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
    )(Env.apply)(Env.unapply)
  )

  // -- Actions

  def index = Action {
    Ok(html.index("Environments Manager"))
  }

  def env(name: String) = Action {
    Ok(html.detailEnv(EnvJson.envs.filter(_.name == name)(0)))
  }

  def envs = Action {
    Ok(EnvJson.jsonValue).as(JSON)
  }

  def reload = Action {
    EnvJson.reload()
    Ok(EnvJson.jsonValue).as(JSON)
  }

  def javascriptRoutes() = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          // Routers
          routes.javascript.Application.env
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
