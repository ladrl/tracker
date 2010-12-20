package scalatoys.tracker.impl.mongodb

import com.mongodb._
import com.osinka.mongodb._
import com.osinka.mongodb.shape._

/*
1 collection of books
1 collection of libs (History, first entry is the current lib)
1 collection of book histories (is a split the same book?)
*/
import java.net.URL
import java.util.Date
import scalatoys.{ tracker => api }
class MongoTracker(val host: String, val trackerName: String) extends api.Tracker {
	
	lazy val db = new Mongo(host).getDB(trackerName)
	lazy val bookColl = db.getCollection("books") of MongoBook
	lazy val libColl = db.getCollection("libs") of MongoLibrary
	lazy val bookHistoryColl = db.getCollection("bookHist")
	
	type Page      = MongoPage
	type HeadLine  = MongoHeadLine
	type Book      = MongoBook
	type Library   = MongoLibrary
	type Copier    = MongoCopier
	type Command   = MongoCommand
	type Catalogue = MongoCatalogue
	
	def Page(content: String, createdBy: String, createdAt: Date) = new MongoPage(content, createdBy, createdAt)
	def HeadLine(content: String, createdBy: String, createdAt: Date) = new MongoHeadLine(content, createdBy, createdAt)
	def Book(pages: Seq[MongoPage], frontPage: Map[String, MongoHeadLine]) = new MongoBook(pages, frontPage)
	def Library(name: String, content: Seq[MongoBook]) = new MongoLibrary(name, content.toList)
	def Library(name: String) = {
		val query = MongoLibrary where { MongoLibrary.name is name} //sortBy MongoLibrary.seqNo ascending
		(query in libColl take 1).headOption
	}
	def Copier() = new MongoCopier(new MongoCommand(EmptyBook, Nil, Map(), Nil))
	val EmptyBook = new MongoBook(Nil, Nil)

	case class MongoPage(val content: String, val createdBy: String, val createdAt: Date) extends A_Page with MongoObject

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
	
	object MongoPage extends ObjectShape[MongoPage] with MongoPageIn[MongoPage]

	case class MongoHeadLine(val content: String, val createdBy: String, val createdAt: Date) extends A_HeadLine with MongoObject

	trait MongoHeadLineIn[T] extends ObjectIn[MongoHeadLine, T] {
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
	
	object MongoHeadLine extends ObjectShape[MongoHeadLine] with MongoHeadLineIn[MongoHeadLine]

	case class MongoBook(val pages: Seq[MongoPage], val frontPage: Map[String, MongoHeadLine]) extends A_Book with MongoObject
	}

	object MongoBook extends MongoObjectShape[MongoBook] { 
		val shape = this
		
		object pages extends MongoArray[MongoPage] with ArrayFieldModifyOp[MongoPage] with EmbeddedContent[MongoPage] with MongoPageIn[MongoBook] {
			override val mongoFieldName = "pages"
			override val rep = shape.Represented.by[Seq[MongoPage]](_.pages,None) // No setter
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

	case class MongoLibrary(val name: String, val books: Seq[MongoBook], val seqNo: Long = 0) extends A_Library with MongoObject {
		def place(book: MongoBook) = {
			val newLib = new MongoLibrary(name, books :+ book, seqNo + 1)
			libColl += newLib
			newLib
		}
		def removeBook(book: MongoBook) = {
			if(books.find(_ == book) == None) error("Unable to find book %s" format book)
			val newLib = new MongoLibrary(name, books filter { _ != book }, seqNo + 1)
			libColl += newLib
			newLib
		}
		def replace(book: MongoBook, by: MongoBook) = removeBook(book).place(by)
		def catalogue: Catalogue = new MongoCatalogue(this)
	}
	object MongoLibrary extends MongoObjectShape[MongoLibrary] {
		val name = Field.scalar("name", _.name)
		val books = Field.arrayRef("books", bookColl, _.books)
		val seqNo = Field.scalar("seqNo", _.seqNo)
		val * = List(name, books, seqNo)
		def factory(dbo: DBObject) = for{
			name(n) <- Option(dbo)
			books(b) <- Option(dbo)
			seqNo(no) <- Option(dbo)
		} yield new MongoLibrary(n, b, no)
	}

	case class MongoCommand(val template: MongoBook, val PlusPages: Seq[MongoPage], val PlusFrontPage: Map[String, MongoHeadLine], val MinusFrontPage: List[String]) extends A_Command {
		def write(pages: Seq[MongoPage]): MongoCommand = copy(PlusPages = PlusPages ++ pages)
		def write(lines: Map[String, MongoHeadLine]): MongoCommand = copy(PlusFrontPage = PlusFrontPage ++ lines)
		def erase(lines: Seq[String]): MongoCommand = copy(MinusFrontPage = MinusFrontPage ++ lines)
		def asNewBook: MongoBook = {
			val newBook = new MongoBook(template.pages ++ PlusPages, (template.frontPage ++ PlusFrontPage).filterKeys { !MinusFrontPage.contains(_) })
			bookColl += newBook
			newBook
		}
	}
	
	class MongoCatalogue(lib: MongoLibrary) extends A_Catalogue {
		def query: Seq[MongoBook] = lib.books
		def query(predicate: MongoBook => Boolean): Seq[MongoBook] = lib.books filter predicate
	}

	class MongoCopier(val command: MongoCommand) extends A_Copier {
		def from(template: MongoBook): MongoCopier = new MongoCopier(new MongoCommand(template, Nil, Map(), Nil))
	}
}