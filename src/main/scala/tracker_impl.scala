package scalatoys.tracker.impl

import scalatoys.tracker._
import java.util.Date


object EmptyBook extends Book {
	override val pages = Nil
	override val frontPage = Map[String, HeadLine]()
}


trait Factory {
	def newPage(content: String, user: User): Page
	def newHeadLine(content: String, user: User): HeadLine
	def newBook(pages: Seq[Page], frontPage: Map[String, HeadLine]): Book
	def newLibrary(content: Seq[Book]): Library
	def newCopier(): Copier
}

object Factory {
	def Page(content: String, user: User)(implicit factory: Factory)  = factory.newPage(content, user)
	def HeadLine(content: String, user: User)(implicit factory: Factory)  = factory.newHeadLine(content, user)
	def Book(pages: Seq[Page], frontPage: Map[String, HeadLine])(implicit factory: Factory)  = factory.newBook(pages, frontPage)
	def Library(content: Seq[Book])(implicit factory: Factory)  = factory.newLibrary(content)
	def Copier(implicit factory: Factory) = factory.newCopier()
}


package simple {
	
	object SimpleFactory extends Factory {
		def newPage(content: String, user: User) = SimplePage(content, user)
		def newHeadLine(content: String, user: User) = SimpleHeadLine(content, user)
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

	case class SimpleHeadLine(val content: String, implicit val createdBy: User, val createdAt: Date = new java.util.Date) extends HeadLine

	case class SimplePage(val content: String, implicit val createdBy: User,  val createdAt: Date = new java.util.Date) extends Page

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
