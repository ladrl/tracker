package scalatoys.tracker.impl.mongodb

import scalatoys.tracker._


class MongoDBTest extends TrackerTest {
	import com.mongodb._
	
	val host = "localhost"
	val dbname = "test"
	
	new Mongo(host).dropDatabase(dbname)
	
	val scope = new MongoTracker(host, dbname)
	test
}