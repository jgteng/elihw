import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/program/m2/repository",
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val scalaTest =  "org.scalatest" %% "scalatest" % "2.0.RC1"
  val twitterUtil = "com.twitter" %% "util-eval" % "6.3.6" withSources() withJavadoc()
  var akka_actor = "com.typesafe.akka" %% "akka-actor" % "2.2.3"
  var akka_slf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.2.3"
  var akka_testkit = "com.typesafe.akka" %% "akka-testkit" % "2.2.3"
  val whale_common = "com.jd.bdp" % "whale-common" % "1.0-SNAPSHOT"
  val whale_communication = "com.jd.bdp" % "whale-common" % "1.0-SNAPSHOT"
  val glowworm = "com.jd.dd" % "glowworm" % "1.0-SNAPSHOT"
}