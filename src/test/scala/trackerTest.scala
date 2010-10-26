package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import java.net.URI

class TrackerTest extends WordSpec with MustMatchers {
	val user = new impl.simple.SimpleUser("lukas")
	"A unique name" should {
		"create an incrementing URI" in {
			val naming = new UniqueNaming {}
			naming.incrementing("%i") must be (new URI("0"))
			naming.incrementing("#%i") must be (new URI("#0"))
			naming.incrementing("#%i") must be (new URI("#1"))
		}
	}
	
	"A tracker" should {
		import impl.simple._
		
		val tracker = new SimpleTracker("TestTracker")
		"create an entry" in {
			val entry = tracker create
			
			entry must be (new SimpleEntry(name = "entry#0"))
		}
		"update an entry" in {
			val entry = tracker create
			
			val modEntry = entry append SimpleContent("test content", user)
			
			tracker update modEntry
			(tracker queryName entry.name) must be (List(modEntry))
		}
		"remove an entry" in {
			val entry = tracker create
			
			(tracker queryName entry.name) must be (List(entry))
			
			tracker remove entry
			
			(tracker queryName entry.name) must be (List())
		}
		
		"try to update a non-existing entry" in {
			val entry = tracker create
			val non_existing = SimpleEntry("invalid")
			
			(tracker remove non_existing) must be (false)
			
			(tracker remove entry) must be (true)
		}
		
		"get an entry" in {
			val entry = tracker create
			
			(tracker queryName entry.name) must be (List(entry))
		}
		
		"get no entry when quering for name ''" in {
			val tracker = new SimpleTracker("TestTracker")
			(0 until 10) foreach { i => tracker create }
			
			(tracker queryName("".r)) must be(List())
		}
		"get all entries when quering for name .*" in {
			val tracker = new SimpleTracker("TestTracker")
			(0 until 10) foreach { i =>  tracker create }
			
			(tracker queryName(""".*""".r) sortWith { (e1, e2) => e1.name < e2.name }) must be(
				(
					((0 until 10).map{i => SimpleEntry("entry#%d" format i, Nil, Map(), SimpleStates.Open)}.toList).sortWith { (e1, e2) => e1.name < e2.name }
				)
			)
		}
		"""get a subset of all entries with name entry#\d""" in {
			val tracker = new SimpleTracker("TestTracker")
			(0 until 20) foreach { i =>  tracker create }
			(tracker queryName("""entry#\d""".r) sortWith { (e1, e2) => e1.name < e2.name}) must be(
				((0 until 10).map{i => SimpleEntry("entry#%d" format i, Nil, Map(), SimpleStates.Open)}.toList).sortWith { (e1, e2) => e1.name < e2.name }	
			) 
		}
	}
	
	"An entry" should {
		import impl.simple._
		val entry = new SimpleEntry(name = "test", entryContent = Nil, entryNamedContent = Map(), entryState = SimpleStates.Open)

		"add content" in {
			val testContent = new SimpleContent("test content", user) :: Nil
			val newEntry = entry.content ++= testContent
			newEntry.content must be (testContent)
		}
		"add metadata" in {
			val newMetaData = "Name" -> new SimpleContent("test meta data", user)
			val newEntry = entry.namedContent += newMetaData
			newEntry.namedContent("Name") must be (newMetaData._2)
		}
		"allow valid state changes" in {
			val newEntry = entry.state = SimpleStates.Closed
			newEntry.state must be (SimpleStates.Closed)
		}
		"disallow invalid state changes" in {
			val newEntry = entry.state = SimpleStates.Unreachable
			newEntry.state must be (SimpleStates.Open)
			
		}
	}
}