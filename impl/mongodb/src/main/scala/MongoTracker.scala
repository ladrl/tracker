package scalatoys.tracker.impl.mongodb

import com.mongodb._
import com.osinka.mongodb._
import com.osinka.mongodb.shape._

import java.net.URL
import java.util.Date
import scalatoys.tracker.{
	Page,
	HeadLine,
	Book,
	Library,
	Catalogue,
	Copier
}
import scalatoys.tracker.{
	Factory
}
/*
1 collection of books
1 collection of libs (History, first entry is the current lib)
1 collection of book histories (is a split the same book?)
*/

case class TrackerId(val host: URL, val trackerName: String) {
	lazy val db = new Mongo(host.toString).getDB(trackerName)
	lazy val bookColl = db.getCollection("books")
	lazy val libColl = db.getCollection("libs")
	lazy val bookHistoryColl = db.getCollection("bookHist")
}

/*
object MongoFactory extends Factory {
	def newPage(content: String, createdBy: String, createdAt: Date) = new MongoPage(content, createdBy, createdAt)
	def newHeadLine(content: String, createdBy: String, createdAt: Date) = new MongoHeadLine(content, createdBy, createdAt)
	def newBook(pages: Seq[MongoPage], frontPage: Map[String, MongoHeadLine]) = new MongoBook(pages, frontPage)
	def newLibrary(content: Seq[Book]) = new MongoLibrary(content.toList)
	def newCopier() = MongoCopier
}
*/

class MongoPage(val content: String, val createdBy: String, val createdAt: Date) extends Page with MongoObject
object MongoPage extends ObjectShape[MongoPage] with MongoPageIn[MongoPage]
trait MongoPageIn[T] extends ObjectIn[MongoPage, T] {
	lazy val content = Field.scalar("content", _.content)
	lazy val createdBy = Field.scalar("createdBy", _.createdBy)
	lazy val createdAt = Field.scalar("createdAt", _.createdAt)
	val * = content :: createdBy :: createdAt :: Nil
	def factory(dbo: DBObject) = 
		for{
			content(c) <- Option(dbo)
			createdBy(by) <- Option(dbo)
			createdAt(at) <- Option(dbo)
		}
		yield new MongoPage(c, by, at)
}

class MongoHeadLine(val content: String, val createdBy: String, val createdAt: Date) extends HeadLine with MongoObject
object MongoHeadLine extends MongoObjectShape[MongoHeadLine] {
	lazy val content = Field.scalar("content", _.content)
	lazy val createdBy = Field.scalar("createdBy", _.createdBy)
	lazy val createdAt = Field.scalar("createdAt", _.createdAt)
	val * = content :: createdBy :: createdAt :: Nil
	def factory(dbo: DBObject) = 
		for{
			content(c) <- Option(dbo)
			createdBy(b) <- Option(dbo)
			createdAt(a) <- Option(dbo)
			}
			yield new MongoHeadLine(c, b, a)
}

class MongoBook(val pages: Seq[MongoPage], val frontPage: Map[String, MongoHeadLine]) extends Book with MongoObject
object MongoBook extends MongoObjectShape[MongoBook] { shape => 
	object pages extends MongoArray[MongoPage] with EmbeddedContent[MongoPage] with MongoPageIn[MongoBook] {
		override val mongoFieldName = "pages"
		override val rep = shape.Represented.by[Seq[MongoPage]](
			_.pages,
			None // No setter
		)
	}
	val frontPage = Field.scalar("frontPage", _.frontPage)
	val * = pages :: frontPage :: Nil
	def factory(dbo: DBObject) = 
		for{
			pages(p) <- Option(dbo)
			frontPage(fp) <- Option(dbo)
		}
		yield new MongoBook(p, fp)
}

class MongoLibrary(val books: List[MongoBook]) extends Library with MongoObject {
	def place(book: MongoBook) = {
		val newLib = new MongoLibrary(books :+ book)
		newLib
	}
	def removeBook(book: MongoBook) = {
		if(books.find(_ == book) == None) error("Unable to find book %s" format book)
		new MongoLibrary(books filter { _ != book })
	}
	def replace(book: MongoBook, by: MongoBook) = removeBook(book).place(by)
	def catalogue: Catalogue = null
}

object MongoCopier extends Copier {
	
}
