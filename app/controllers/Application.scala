package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._

import play.api.templates.Html

import _root_.db.Browser
import util.{JsonUtil, Config}
import models.Env.extVhostFormat

import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import com.ning.http.client.Realm
import play.api.Routes

object Application extends Controller {
  private val ALL = "All"
  val TITLE: String = "Environment Manager"

  // -- Authentication

  val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text
    ) verifying("Invalid login or password", {
      case (login, password) => User.authenticate(login, password)
    })
  )

  /**
   * Login page.
   */
  def login = Action {
    implicit request =>
      Ok(html.login(TITLE, loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(TITLE, formWithErrors)),
        user => Redirect(routes.Projects.index()).withSession("login" -> user._1)
      )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login()).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

  def index = Action {
    Ok(html.index(TITLE))
  }

  def serverDetail(serverName: String) = Action {
    Ok(
      if (serverName == ALL) Html("")
      else html.serverDetail(Env.server(serverName))
    )
  }

  def vhostDetail(serverName: String, name: String, isVpn: String) = Action {
    Ok(
      html.vhostDetail(Env.vhost(serverName, name), isVpn=="1")
    )
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

  val svnLogin = Config.get("svn.login", "login")
  val svnPsw = Config.get("svn.password", "password")
  val svnUrl = Config.get("svn.url", "http://svn.com")

  def bpByName(name: String, dir: String, ext: String) = Action {
    Async {
      val fullDir = s"$svnUrl/$dir/"
      WS.url(fullDir).
        withAuth(svnLogin, svnPsw, Realm.AuthScheme.BASIC).get().map {
        response => {
          val pattern = s"$name$ext".r
          pattern findFirstIn response.body match {
            case Some(nameWithVersion) =>
              Redirect(fullDir + nameWithVersion)
            case None => BadRequest("Process with name '" + name + "' not found")
          }
        }
      }
    }
  }

  def procByName(name: String) = bpByName(name, "local_default_My%20Processes", "_diagram_.+?\\.proc")

  def barByName(name: String) = bpByName(name, "barBonitaProcess", "--.+?\\.bar")
}

/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user login.
   */
  private def username(request: RequestHeader) = request.session.get("login")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login())

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
    user =>
      Action(request => f(user)(request))
  }
}
