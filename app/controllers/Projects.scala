package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import models._
import views._

/**
 * Manage projects related operations.
 */
object Projects extends Controller with Secured {

  /**
   * Display the main page.
   */
  def index = IsAuthenticated { username => _ =>
      Ok(
        html.index(Application.TITLE)
      )
  }
}

