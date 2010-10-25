import sbt._

class TrackerBuild(info: ProjectInfo) extends DefaultProject(info) with growl.GrowlingTests {
	val scalaToolsSnapshots = ScalaToolsSnapshots
	val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
	
	//val cli = "commons-cli" % "commons-cli" % "1.2"
}