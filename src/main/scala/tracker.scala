// Tracker
package scalatoys.tracker

import java.net.{URI, URL}
import java.util.Date
import scala.util.matching.Regex

trait Named {
	def name: String
}

// wär het's mir gseit?
trait User extends Named {
	def realName: Option[String]
	def passwordHash: Option[List[Byte]]
	// Additonal user data, i.e. password sha, real name, email, etc.
}

// Tagebuchseite
trait Content {
//	def createdBy: User
	def createdAt: Date
	/*
	def modifiedAt: Option[Date]
	def modifiedBy: Option[User]
	*/
	def toString: String
}

// Lebensabschnitt
trait State extends Named {
	type Transit = PartialFunction[State, Unit]
	def canTransitTo(state: State) = transits.isDefinedAt(state)
	val transits: Transit
}

// Tagebuch eines "Bugs" :-)
trait Entry extends Named {
	type Meta = Map[String, Content]

	def content: List[Content]
	// Entry <- content <- content
	def <-
//	def content_=(replaceWith: List[Content]): Unit

	def meta: Meta
	def meta_=(replaceWith: Meta): Unit

	def state: State
	def state_=(newState: State): Unit
}

// Tagebuchbibliothek
trait Tracker {
	def create(): Entry
	def update(what: Entry): Entry
	def remove(what: Entry): Boolean
	def query(predicate: Entry => Boolean): List[Entry]
	
	def queryName(name: String): List[Entry] = queryName(name.r)
	def queryName(name: Regex): List[Entry] = query { e:Entry => name.pattern.matcher(e.name).matches }
}

// Lebensplanung
trait TrackerConfiguration {
	def states(): List[State]
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

object Tracker {
	import impl.simple._
	val Simple = """Simple (.+)""".r
	def create(name: String):Tracker = name match {
		case Simple(name) => new SimpleTracker(name)
	}
}


package impl {
	/*
	package db {
		import com.mongodb._
		import com.osinka.mongodb._
		import com.osinka.mongodb.shape._
		
		class MongoUser(val name: String) extends User with MongoObject {
			var realName: Option[String] = None
			var passwordHash: Option[List[Byte]] = None
		}
		object MongoUser extends MongoObjectShape[MongoUser] {
			lazy val name = Field.scalar("name", _.name)
			lazy val realName = Field.optional("realName", _.realName, (x: MongoUser, v: Option[String]) => x.realName = v)
//			lazy val passwordHash = Field.optional("passwordHash", _.passwordHash, (x: MongoUser, v: Option[List[Byte]]) => x.passwordHash = v)
			override lazy val * = name :: realName :: passwordHash :: Nil
			override def factory(dbo: DBObject) = for{
					name(n) <- Some(dbo)
				}
				yield new MongoUser(n)
		}
		
		class SimpleContent(val createdBy: User, val createdAt: Date, var data: String) extends Content {
			override def toString = "from %s by %s: %s" format (createdBy, createdAt, data)
		}
		
		class MongoContent(val createdBy: User, val createdAt: Date) extends Content with MongoObject {
			var data: String = _
			override def toString: String = data
		}
		
		object MongoContent extends MongoObjectShape[MongoContent] {
			lazy val createdBy = Field.scalar("createdBy", _.createdBy)
			lazy val createdAt = Field.scalar("createdAt", _.createdAt)
			lazy val data = Field.scalar("data", _.data, (x: MongoContent, v: String) => x.data = v)
			override lazy val * = List(createdBy, createdAt, data)
			override def factory(dbo: DBObject) = for{
					createdBy(by) <- Some(dbo)
					createdAt(at) <- Some(dbo)
				}
					yield new MongoContent(by, at)
		}
		
		
		class MongoEntry(val name: String) extends Entry with MongoObject {
			var content: List[Content] = _
			var namedContent: NamedContent = _
			def state():State = {
				null
			}
			def state_=(newState: State) {
				
			}
		}
		object MongoEntry extends MongoObjectShape[MongoEntry]{
			lazy val content = Field.array("content", _.content, (x: MongoEntry, v: Seq[Content]) => x.content = v.toList)
			override lazy val * = content :: Nil
			override def factory(dbo: DBObject) = None
		}
		
		class MongoTracker (val name: String) extends Tracker with MongoObject {
			val trackerName = "tracker %s" format name

			def create(): Entry = {
				null
			}

			def update(what: Entry) : Entry = {
				what
			}

			def remove(what: Entry) : Boolean = {
				false
			}

			def query(predicate: Entry => Boolean): List[Entry] = {
				Nil
			}
		}
	}
	*/
	package simple {
		class SimpleUser(val name: String) extends User {
			def uniqueId = new URI("user_%s" format (name.filter(_ != ' ')))
			def realName: Option[String] = None
			def passwordHash: Option[List[Byte]] = None
		}

		case class SimpleContent(text: String, 
			val createdBy: User, val createdAt: Date = new Date(), 
			val modifiedBy: Option[User] = None, val modifiedAt: Option[Date] = None) extends Content {
				override def toString = text
			}

			case class SimpleEntry(
				val name: String, 
				val entryContent: List[Content] = Nil, 
				val entryNamedContent: Entry#Meta = Map(),
				private val entryState: State = SimpleStates.Open
				) extends Entry {

					def uniqueId = new URI("entry_%s" format (name.filter(_ != ' ')))

					def content: List[Content] = entryContent
					def content_=(replaceWith: List[Content]) = copy(entryContent = replaceWith)

					def meta: Meta = entryNamedContent
					def meta_=(replaceWith: Meta) = copy(entryNamedContent = replaceWith)

					def state: State = entryState

					def state_=(newState: State) { 
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

				abstract class AbstractSimpleTracker(val name: String, var entries: Map[String, Entry], val uniqueId: URI)  extends Tracker with UniqueNaming {
					def create(): Entry = {
						val newEntry = new SimpleEntry(incrementing("entry#%i").toString, Nil, Map(), SimpleStates.Open)
						entries += (newEntry.name -> newEntry)
						newEntry
					}

					def update(what: Entry) = {
						if(! entries.contains(what.name)) error("Trying to update an unknown entry")
						entries += what.name -> what
						entries(what.name)
					}

					def query(predicate: (Entry) => Boolean) = (entries map { _._2 }filter predicate).toSet.toList

					def backup(to: URL) { }

					def remove(what: Entry) = {
						if(entries contains what.name)
						{
							entries = entries.filterKeys { _ != what.name }
							true
						}
						else
						false
					}	
				}

				class SimpleTracker(name: String) extends AbstractSimpleTracker(name, Map(), new URI("tracker_%s" format (name.filter{_ != ' '})))
	}
}
