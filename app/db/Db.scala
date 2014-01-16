package db

import java.sql.{Connection, DriverManager}
import models.{Env, VHost}

/**
 * User: gkislin
 * Date: 24.06.13
 *
 * @see http://www.jaredarmstrong.name/2012/02/play-2-0-anorm-standalone/
 */

object Db {

  // Quick way to load the driver
  Class.forName("org.postgresql.Driver").newInstance

  def getConnection(vhost: VHost) = {
    getConnectionByUrl(Env.psqlExt(vhost, vhost.vpnIp!=None))
  }

  def getConnectionByUrl(url: String) = {
    DriverManager.getConnection(url)
  }

  def withConnection[A](vhost: VHost)(block: Connection => A): A = {
    val connection = getConnection(vhost)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }

  def withTransaction[A](vhost: VHost)(block: Connection => A): A = {
    withConnection(vhost) {
      connection =>
        try {
          connection.setAutoCommit(false)
          val r = block(connection)
          connection.commit()
          r
        } catch {
          case e: Throwable => connection.rollback(); throw e
        }
    }
  }
}
