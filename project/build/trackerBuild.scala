import sbt._

class TrackerBuild(info: ProjectInfo) extends DefaultProject(info) with growl.GrowlingTests {
	val scalaToolsSnapshots = ScalaToolsSnapshots
	val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
	
//	val db = "org.neodatis" % "neodatis-odb" % "1.9-beta-1" % "compile"
	val mongo = "com.osinka" % "mongo-scala-driver_2.8.0" % "0.8.2"
}