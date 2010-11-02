import sbt._

class TrackerBuild(info: ProjectInfo) extends DefaultProject(info) with growl.GrowlingTests {
	val scalaToolsSnapshots = ScalaToolsSnapshots
	val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
	
	val mongo = "com.osinka" % "mongo-scala-driver_2.8.0" % "0.8.2"
}