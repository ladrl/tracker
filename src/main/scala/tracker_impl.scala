package scalatoys.tracker.impl

import scalatoys.tracker._
import java.util.Date


object EmptyBook extends Book {
	override val pages = Nil
	override val frontPage = Map[String, HeadLine]()
}


trait Factory {
	def newPage(content: String, user: String): Page
	def newHeadLine(content: String, user: String): HeadLine
	def newBook(pages: Seq[Page], frontPage: Map[String, HeadLine]): Book
	def newLibrary(content: Seq[Book]): Library
	def newCopier(): Copier
}

object Factory {
	def Page(content: String, user: String)(implicit factory: Factory)  = factory.newPage(content, user)
	def HeadLine(content: String, user: String)(implicit factory: Factory)  = factory.newHeadLine(content, user)
	def Book(pages: Seq[Page], frontPage: Map[String, HeadLine])(implicit factory: Factory)  = factory.newBook(pages, frontPage)
	def Library(content: Seq[Book])(implicit factory: Factory)  = factory.newLibrary(content)
	def Copier(implicit factory: Factory) = factory.newCopier()
}

/*
package mongodb {
	import com.mongodb._
	import com.osinka.mongodb._
	
	object MongoFactory extends Factory {
		def newPage(content: String, user: User) = new MongoPage(content, user)
		def newHeadLine(content: String, user: User) = new MongoHeadLine(content, user)
		def newBook(pages: Seq[Page], frontPage: Map[String, HeadLine]) = new MongoBook(pages.toList, frontPage)
		def newLibrary(content: Seq[Book]) = new MongoLibrary(content.toList)
		def newCopier() = MongoCopier
	}
	
	class MongoPage(val content: String, user: User) extends Page with MongoObject
	object MongoPage extends MongoObjectShape[MongoPage] {
		lazy val content = Field.scalar("content", _.content)
		lazy val user = Field.ref("createdBy", _.user)
		val * = content :: user :: Nil
		def factory(dbo: MongoObject) = 
			for{
				content <- Option(dbo)
				user <- Option(dbo)
				}
				yield new MongoPage(content, user)
	}
	
	class MongoHeadLine(val content: String, user: User) extends HeadLine with MongoObject
	object MongoHeadLine extends MongoShape[MongoHeadLine] {
		lazy val content = Field.scalar("content", _.content)
		lazy val user = Field.ref("createdBy", _.user)
		val * = content :: user :: Nil
		def factory(dbo: MongoObject) = 
			for{
				content <- Option(dbo)
				user <- Option(dbo)
				}
				yield new MongoHeadLine(content, user)
	}
	
	class MongoBook(val pages: List[Page], val frontPage: Map[String, HeadLine]) extends Book with MongoObject
	object MongoBook extends MongoShape[MongoBook] {
	}
	
	class MongoLibrary(val books: List[Book]) extends Library with MongoObject
	
	object MongoCopier extends Copier {
		
	}
}
*/
package simple {
	
	object SimpleFactory extends Factory {
		def newPage(content: String, user: String) = SimplePage(content, user)
		def newHeadLine(content: String, user: String) = SimpleHeadLine(content, user)
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
		def query(predicate: Book => Boolean): List[Book] = {
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
