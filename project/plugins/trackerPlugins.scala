import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val lessRepo = "lessis repo" at "http://repo.lessis.me"
  val growl = "me.lessis" % "sbt-growl-plugin" % "0.0.5"
  
  val jawsyMavenReleases = "Jawsy.fi M2 releases" at "http://oss.jawsy.fi/maven2/releases"
  val jrebelPlugin = "fi.jawsy" % "sbt-jrebel-plugin" % "0.2.1"
}