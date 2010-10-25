// Tracker
package scalatoys.tracker

import java.net.{URI, URL}
import java.util.Date
import scala.util.matching.Regex

trait UniqueNamed {
	def uniqueId: URI
	def name: String
}

trait User extends UniqueNamed {
	def realName: Option[String] = None
	def passwordHash: Option[Seq[Byte]] = None
	// Additonal user data, i.e. password sha, real name, email, etc.
}

trait Content {
	def createdAt: Date
	def modifiedAt: Option[Date]
	def createdBy: User
	def modifiedBy: Option[User]
}

trait State extends UniqueNamed {
	type Transit = PartialFunction[State, Unit]
	def canTransitTo(state: State) = transits.isDefinedAt(state)
	val transits: Transit
}

trait TrackerEntry extends UniqueNamed {
	type NamedContent = Map[String, Content]

	def content: List[Content]
	def content_=(replaceWith: List[Content]): TrackerEntry
	def append(toAppend: Content) = this.content = toAppend :: this.content

	def namedContent: NamedContent
	def namedContent_=(replaceWith: NamedContent): TrackerEntry
	def append(name: String, toAppend: Content) = this.namedContent += name -> toAppend

	private var entryState: State = _
	def state: State
	def state_=(newState: State): TrackerEntry
}

trait Tracker extends UniqueNamed {
	def backup(to: URL)
	
	def create(): TrackerEntry
	def update(what: TrackerEntry): TrackerEntry
	def remove(what: TrackerEntry): Boolean
	def query(predicate: TrackerEntry => Boolean): List[TrackerEntry]
	def queryName(name: String): List[TrackerEntry] = queryName(name.r)
	def queryName(name: Regex): List[TrackerEntry] = query { e:TrackerEntry => name.pattern.matcher(e.name).matches }
}

trait UniqueNaming {
	def incrementing(format: String): URI = {
		def increment(format: String): Int = {
			val currentIncrement = if(incrementingFormats contains format) incrementingFormats(format) else 0
			incrementingFormats(format) = currentIncrement + 1
			currentIncrement
		}
		val formatPattern = """(.*)(%\w)(.*)""".r

		format match {
			case formatPattern(pre, "%i", post) => new URI(pre + increment(format) + post)

		}
	}
	private var incrementingFormats = scala.collection.mutable.Map[String, Int]()
}


package impl {
	
	class SimpleUser(val name: String) extends User {
		def uniqueId = new URI("user_%s" format (name.filter(_ != ' ')))
	}
	
	case class SimpleContent(text: String, 
		val createdBy: User, val createdAt: Date = new Date(), 
		val modifiedBy: Option[User] = None, val modifiedAt: Option[Date] = None) extends Content {
		override def toString = text
	}
	
	case class SimpleEntry(
		val name: String, 
		val entryContent: List[Content] = Nil, 
		val entryNamedContent: TrackerEntry#NamedContent = Map(),
		private val entryState: State = SimpleStates.Open
		) extends TrackerEntry {
			
		def uniqueId = new URI("entry_%s" format (name.filter(_ != ' ')))

		def content: List[Content] = entryContent
		def content_=(replaceWith: List[Content]) = copy(entryContent = replaceWith)

		def namedContent: NamedContent = entryNamedContent
		def namedContent_=(replaceWith: NamedContent) = copy(entryNamedContent = replaceWith)

		def state: State = entryState

		def state_=(newState: State): TrackerEntry = { 
			if(state canTransitTo newState)
				copy(entryState = newState)
			else
				copy()
		}
		
	}
	
	trait SimpleState extends State {
		def uniqueId = new URI(name)
	}
	
	object SimpleStates {
		object Open extends SimpleState {
			val name = "Open"
			val transits: Transit = {
				case Closed => ()
			}
		}

		object Closed extends SimpleState {
			val name = "Closed"
			val transits: Transit = {
				case Open => ()
			}
		}
		
		object Unreachable extends SimpleState {
			val name = "Unreachable"
			val transits: Transit = {
				case _ => ()
			}
		}
	}
	
	import scala.util.logging._
	class SimpleTracker(val name: String) extends Tracker with UniqueNaming with Logged with ConsoleLogger {
		var entries = Map[String, TrackerEntry]()
		val uniqueId =  new URI("tracker_%s" format (name.filter{_ != ' '}))
		def create(): TrackerEntry = {
			val newEntry = new SimpleEntry(incrementing("entry#%i").toString, Nil, Map(), SimpleStates.Open)
			entries += (newEntry.name -> newEntry)
			newEntry
		}
		def update(what: TrackerEntry) = {
			if(! entries.contains(what.name)) error("Trying to update an unknown entry")
			entries += what.name -> what
			entries(what.name)
		}
		
		
		def query(predicate: (TrackerEntry) => Boolean) = (entries map { _._2 }filter predicate).toSet.toList
		
		def backup(to: URL) { }
		def remove(what: TrackerEntry) = {
			if(entries contains what.name)
			{
				entries = entries.filterKeys { _ != what.name }
				true
			}
			else
				false
		}
	}
}
