import sbt._

class TrackerBuild(info: ProjectInfo) extends ParentProject(info) {
	val scalaToolsSnapshots = ScalaToolsSnapshots

	val api  = project("api", "api", new Api(_))
	class Api(info: ProjectInfo) extends DefaultProject(info) {
		val scalatest = "org.scalatest" % "scalatest" % "1.2"
	}
	val impl = project("impl", "impl", new Implementations(_), api)
	val ui   = project("ui", "ui", new UIs(_), api)
	val app  = project("app", "app", new Applications(_), api)
	
	class Implementations(info: ProjectInfo) extends ParentProject(info) {
		val stub = project("stub", "stub", api)

		val mongodb = project("mongodb", "mongodb", new MongoDB(_), api)
		class MongoDB(info: ProjectInfo) extends DefaultProject(info) {
		  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
			val mongo = "com.osinka" % "mongo-scala-driver" % "0.8.9-SNAPSHOT" % "compile"
		}
	}
	
	class UIs(info: ProjectInfo) extends ParentProject(info) {
		val cli = project("cli", "cli", new CLI(_), api)
		class CLI(info: ProjectInfo) extends DefaultProject(info) {
			val jline = "jline" % "jline" % "0.9.94" % "compile"
		}
		val webUI = project("webUI", "webUI", new WebUI(_), api, impl.mongodb)
		import fi.jawsy.sbtplugins.jrebel.JRebelWebPlugin
		class WebUI(info: ProjectInfo) extends DefaultWebProject(info) with JRebelWebPlugin {

       val scalatoolsSnapshot = "Scala Tools Snapshot" at
      "http://scala-tools.org/repo-snapshots/"

      val scalatoolsRelease = "Scala Tools Snapshot" at
      "http://scala-tools.org/repo-releases/"

      val liftVersion = "2.2-RC4"

      val webkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default"
      val mapper = "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default"
      val h2 = "com.h2database" % "h2" % "1.2.138"
      val jettyyy = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default"
      val junit = "junit" % "junit" % "4.5" % "test->default"
      val logback = "ch.qos.logback" % "logback-classic" % "0.9.26"
      val specs = "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"

      val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
      val mongo = "com.osinka" % "mongo-scala-driver" % "0.8.9-SNAPSHOT" % "compile"
    }
	}
	
	class Applications(info: ProjectInfo) extends ParentProject(info) {
		val mongocli = project("mongocli", "mongocli", ui.cli, impl.mongodb)
	}
	

	
//	override def fork = forkRun
}