import sbt._
import Keys._

object BuildSettings {

  lazy val basicSettings = seq(
    version               := "0.1.0-SNAPSHOT",
    organization          := "org elihw",
    startYear             := Some(2013),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion          := "2.10.2",
    resolvers             ++= Dependencies.resolutionRepos
  )

  lazy val managerSettings = basicSettings
  lazy val clientSettings = basicSettings
}
