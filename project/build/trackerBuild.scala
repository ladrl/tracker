import sbt._

class TrackerBuild(info: ProjectInfo) extends ParentProject(info) {
	val scalaToolsSnapshots = ScalaToolsSnapshots

	val api  = project("api", "api", new Api(_))
	class Api(info: ProjectInfo) extends DefaultProject(info) {
		val scalatest = "org.scalatest" % "scalatest" % "1.2"
	}
	val impl = project("impl", "impl", new Implementations(_), api)
	val ui   = project("ui", "ui", new UIs(_), api)
	
	class Implementations(info: ProjectInfo) extends ParentProject(info) {
		val stub = project("stub", "stub", api)

		//val mongodb = project("mongodb", "mongodb", new MongoDB(_))
		class MongoDB(info: ProjectInfo) extends DefaultProject(info) {
			val mongo = "com.osinka" % "mongo-scala-driver_2.8.0" % "0.8.2" % "compile"
		}
	}
	
	class UIs(info: ProjectInfo) extends ParentProject(info) {
		val cli = project("cli", "cli", new CLI(_), api, impl)
		class CLI(info: ProjectInfo) extends DefaultProject(info) {
			val jline = "jline" % "jline" % "0.9.94" % "compile"
		}
	}
	
//	override def fork = forkRun
}