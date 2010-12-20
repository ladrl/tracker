package scalatoys.tracker.app.mongocli

import scalatoys.tracker.impl.mongodb.MongoTracker
import scalatoys.tracker.ui.TrackerCLI

object MongoCLI {
	def main(args: Array[String]) = {
		val host = args(0)
		val name = args(1)
		
		val tracker = new MongoTracker(host, name)
		val cli = new TrackerCLI(tracker)
		cli.run
	}
}