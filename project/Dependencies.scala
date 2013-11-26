import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/program/m2/repository"
//    "Twitter Maven Repo" at "http://maven.twttr.com/",
//    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
//    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  lazy val scalaTest =  "org.scalatest" %% "scalatest" % "2.0.RC1"
  lazy val twitterUtil = "com.twitter" %% "util-eval" % "6.3.6" withSources() withJavadoc()
  lazy val akka_actor = "com.typesafe.akka" %% "akka-actor" % "2.2.3"
  lazy val akka_slf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.2.3"
  lazy val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % "2.2.3"
  lazy val whale_common = "com.jd.bdp" % "whale-common" % "1.0-SNAPSHOT"
  lazy val whale_communication = "com.jd.bdp" % "whale-communication" % "1.0-SNAPSHOT"
  lazy val glowworm = "com.jd.dd" % "glowworm" % "1.0-SNAPSHOT"
  lazy val ini4j = "org.ini4j" % "ini4j" % "0.5.2"
//  lazy val log4j = "log4j" % "log4j" % "1.2.15" exclude("com.sun.jmx", "jmxri") exclude("com.sun.jmx", "jmxtools") exclude("javax.jms", "jms") exclude("javax.mail", "mail")
//  lazy val slf4j_log4j12 = "org.slf4j" % "slf4j-log4j12" % "1.7.2"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.7"
}