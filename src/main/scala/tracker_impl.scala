package scalatoys.tracker.impl

import scalatoys.tracker._
import java.util.Date


object EmptyBook extends Book {
	override val pages = Nil
	override val frontPage = Map[String, HeadLine]()
}


trait Factory {
	def newPage(content: String, createdBy: String, createdAt: Date): P forSome { type P <: Page} 
	def newHeadLine(content: String, createdBy: String, createdAt: Date): HL forSome {type HL <: HeadLine}
	def newBook(pages: Seq[Page], frontPage: Map[String, HeadLine]): B forSome { type B <: Book}
	def newLibrary(content: Seq[Book]): L forSome { type L <: Library}
	def newCopier(): Copier
}

object Factory {
	def Page(content: String, createdBy: String, createdAt: Date)(implicit factory: Factory)  = factory.newPage(content, createdBy, createdAt)
	def HeadLine(content: String, createdBy: String, createdAt: Date)(implicit factory: Factory)  = factory.newHeadLine(content, createdBy, createdAt)
	def Book(pages: Seq[Page], frontPage: Map[String, HeadLine])(implicit factory: Factory)  = factory.newBook(pages, frontPage)
	def Library(content: Seq[Book])(implicit factory: Factory)  = factory.newLibrary(content)
	def Copier(implicit factory: Factory) = factory.newCopier()
}

object DefaultFactory {
	def Page(content: String, createdBy: String, createdAt: Date = new Date)(implicit factory: Factory)  = factory.newPage(content, createdBy, createdAt)
	def HeadLine(content: String, createdBy: String, createdAt: Date = new Date)(implicit factory: Factory)  = factory.newHeadLine(content, createdBy, createdAt)
	def Book(pages: Seq[Page], frontPage: Map[String, HeadLine])(implicit factory: Factory)  = factory.newBook(pages, frontPage)
	def Library(content: Seq[Book])(implicit factory: Factory)  = factory.newLibrary(content)
	def Copier(implicit factory: Factory) = factory.newCopier()
}

package mongodb {
	
	/*
	1 collection of books
	1 collection of libs (History, first entry is the current lib)
	1 collection of book histories (is a split the same book?)
	
	val trackerId = new URL("mongo://<host>/<tracker-name>")
	val trackerDb = trackerId.getHost
	val trackerName = trackerId.getPath
	val db = new Mongo(trackerDb).getDB(tracker)
	val bookColl = db.getCollection("books")
	val libColl = db.getCollection("libs")
	val bookHistoryColl = db.getCollection("bookHist")
	*/
	
	import com.mongodb._
	import com.osinka.mongodb._
	import com.osinka.mongodb.shape._
	
	/*
	
	object MongoFactory extends Factory {
		def newPage(content: String, createdBy: String, createdAt: Date) = new MongoPage(content, createdBy, createdAt)
		def newHeadLine(content: String, createdBy: String, createdAt: Date) = new MongoHeadLine(content, createdBy, createdAt)
		def newBook(pages: Seq[Page], frontPage: Map[String, HeadLine]) = new MongoBook(pages, frontPage)
		def newLibrary(content: Seq[Book]) = new MongoLibrary(content.toList)
		def newCopier() = MongoCopier
	}
	*/
	
	/*
	class MongoPage(val content: String, val createdBy: String, val createdAt: Date) extends Page with MongoObject
	object MongoPage extends ObjectShape[MongoPage] with MongoPageIn[MongoPage] {
		val * = content :: createdBy :: createdAt :: Nil
		def factory(dbo: DBObject) = 
			for{
				content(c) <- Option(dbo)
				createdBy(b) <- Option(dbo)
				createdAt(a) <- Option(dbo)
				}
				yield new MongoPage(c, b, a)
	}
	trait MongoPageIn[T] extends ObjectIn[MongoPage, T] {
		lazy val content = Field.scalar("content", _.content)
		lazy val createdBy = Field.scalar("createdBy", _.createdBy)
		lazy val createdAt = Field.scalar("createdAt", _.createdAt)
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
	}
	
	class MongoLibrary(val books: List[Book]) extends Library with MongoObject
	
	object MongoCopier extends Copier {
		
	}*/
}


package simple {
	
	object SimpleFactory extends Factory {
		def newPage(content: String, createdBy: String, createdAt: Date): SimplePage = SimplePage(content, createdBy, createdAt)
		def newHeadLine(content: String, createdBy: String, createdAt: Date) = SimpleHeadLine(content, createdBy, createdAt)
		def newBook(pages: Seq[Page], frontPage: Map[String, HeadLine]) = SimpleBook(pages.toList, frontPage)
		def newLibrary(content: Seq[Book]) = SimpleLibrary(content.toList)
		def newCopier() = new SimpleCopier()
	}
	
	import Factory._
	
	case class DuplicateBook(
		book: Book, 
		additionalPages: List[Page] = Nil, 
		additionalHeadlines: Map[String, HeadLine] = Map(), 
		subtractionalHeadlines: List[String] = Nil) extends CopierCommand {
			def write(page: Page)                   = copy(additionalPages = additionalPages :+ page)
			def write(line: (String, HeadLine))     = copy(additionalHeadlines = additionalHeadlines + line)
			def write(lines: Map[String, HeadLine]) = copy(additionalHeadlines =  additionalHeadlines ++ lines)
			def erase(line: String)                 = copy(subtractionalHeadlines = subtractionalHeadlines :+ line)
			def erase(lines: List[String])          = copy(subtractionalHeadlines = subtractionalHeadlines ++ lines)

			def asNewBook: Book = SimpleBook(
				book.pages ++ additionalPages,
				(book.frontPage ++ additionalHeadlines).filterKeys { !subtractionalHeadlines.contains(_) }
				)
			}
			
	case class SimpleBook(val pages: List[Page], val frontPage: Map[String, HeadLine]) extends Book

	case class SimpleHeadLine(val content: String, val createdBy: String, val createdAt: Date = new java.util.Date) extends HeadLine

	case class SimplePage(val content: String, val createdBy: String,  val createdAt: Date = new java.util.Date) extends Page

	class SimpleCopier(val book: Book = EmptyBook, val command: Option[CopierCommand] = None) extends Copier {
		def from(template: Book) = new SimpleCopier(template, Some(DuplicateBook(template)))
	}
	
	case class SimpleCatalogue(val toQuery: SimpleLibrary) extends Catalogue {
		def query: Seq[Book] = {
			toQuery books
		}
		def query(predicate: Book => Boolean): Seq[Book] = {
			for(book <- toQuery.books if predicate(book))
				yield book
		}
	}
	
	case class SimpleLibrary(val books: List[Book]) extends Library {
		def place(book: Book) = new SimpleLibrary(books :+ book)
		def removeBook(book: Book) = {
			if(books.find(_ == book) == None) error("Unable to find book %s" format book)
			new SimpleLibrary(books filter { _ != book })
		}
		def replace(book: Book, by: Book) = removeBook(book).place(by)
		def catalogue: Catalogue = new SimpleCatalogue(this)

		override def toString = "Library with Books %s" format(books.toString)
	}
}
