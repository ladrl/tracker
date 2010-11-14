package scalatoys.tracker.impl

import scalatoys.tracker._
import java.util.Date
package simple {

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

	object EmptyBook extends Book {
		override val pages = Nil
		override val frontPage = Map[String, HeadLine]()
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
