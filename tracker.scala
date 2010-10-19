// Tracker
package scalatoys.tracker

trait UniqueNamed {
	def uniqueId: URI
	def name: String
}

trait User extends UniqueNamed {
	// Additonal user data, i.e. password sha, real name, email, etc.
}
	
trait Content {
	def createdAt: Date
	def modifiedAt: Date
	def createdBy: User
	def modifiedBy: User
}

trait MetaDataEntry extends Content {
}

type MetaData = Map[String, MetaDataEntry]

trait State extends UniqueNamed {
	def canTransitTo(other: State): Bool
}

trait TrackerEntry extends UniqueNamed {
	
	def destroy: Unit
	
	def content: Seq[Content]
	def content_=(Seq[Content])
	
	def metadata: MetaData
	def metadata_=(MetaData)
	
	def state: State
	def state_=(State): Unit
}

trait Location extends java.net.URL

trait Tracker extends UniqueNamed {
	def init(loc: Location)
	def backup(to: Location)
	
	def create: TrackerEntry
	def query(): List[TrackerEntry]
}
	

