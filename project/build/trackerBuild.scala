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
			val mavenLocal = "Local Maven Repository" at  "file://"+Path.userHome+"/.m2/repository"
			val mongo = "com.osinka" % "mongo-scala-driver" % "0.8.9-SNAPSHOT" % "compile"
		}
	}
	
	class UIs(info: ProjectInfo) extends ParentProject(info) {
		val cli = project("cli", "cli", new CLI(_), api)
		class CLI(info: ProjectInfo) extends DefaultProject(info) {
			val jline = "jline" % "jline" % "0.9.94" % "compile"
		}
	}
	
	class Applications(info: ProjectInfo) extends ParentProject(info) {
		val mongocli = project("mongocli", "mongocli", ui.cli, impl.mongodb)
		
	}
	
//	override def fork = forkRun
}