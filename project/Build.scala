import sbt._
import sbt.Keys._
import play.Project._
import scala.Some

object ApplicationBuild extends Build {

    val appName         = "EnvM"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
//      scalaVersion := "2.10.1-local",
//      autoScalaLibrary := false,
//      scalaHome := Some(file("d:/scala/scala-2.10.1"))
    )

}
