package scalatoys.tracker.impl.simple

import java.util.Date
import scalatoys.tracker.Tracker
	class Simple extends Tracker {
		type Page      = SimplePage
		type HeadLine  = SimpleHeadLine
		type Book      = SimpleBook
		type Library   = SimpleLibrary
		type Copier    = SimpleCopier
		type Command   = SimpleCommand
		type Catalogue = SimpleCatalogue
	
		def Page(content: String, createdBy: String, createdAt: Date) = SimplePage(content, createdBy, createdAt)
		def HeadLine(content: String, createdBy: String, createdAt: Date) = SimpleHeadLine(content, createdBy, createdAt)
		def Book(pages: Seq[SimplePage], frontPage: Map[String, SimpleHeadLine]) = SimpleBook(pages, frontPage)
		def Library(content: Seq[SimpleBook]) = new SimpleLibrary(content.toList)
		def Copier() = new SimpleCopier()
		val EmptyBook = new SimpleBook(Nil, Map[String, SimpleHeadLine]())
	
		case class SimpleCommand(
			book: SimpleBook, 
			additionalPages: List[SimplePage] = Nil, 
			additionalHeadlines: Map[String, SimpleHeadLine] = Map(), 
			subtractionalHeadlines: List[String] = Nil
		) extends A_Command {
			def write(pages: List[SimplePage]): SimpleCommand    = copy(additionalPages = additionalPages ++ pages)
			def write(lines: Map[String, SimpleHeadLine]): SimpleCommand = copy(additionalHeadlines =  additionalHeadlines ++ lines)
			def erase(lines: List[String]): SimpleCommand          = copy(subtractionalHeadlines = subtractionalHeadlines ++ lines)
			def asNewBook: SimpleBook = SimpleBook(
				book.pages ++ additionalPages,
				(book.frontPage ++ additionalHeadlines).filterKeys { !subtractionalHeadlines.contains(_) }
				)
		}

 	
		case class SimpleBook(val pages: Seq[SimplePage], val frontPage: Map[String, SimpleHeadLine]) extends A_Book

		case class SimpleHeadLine(val content: String, val createdBy: String, val createdAt: Date = new java.util.Date) extends A_HeadLine

		case class SimplePage(val content: String, val createdBy: String,  val createdAt: Date = new java.util.Date) extends A_Page

		class SimpleCopier(val book: SimpleBook = EmptyBook, val command: SimpleCommand = SimpleCommand(EmptyBook)) extends A_Copier {
			def from(template: SimpleBook) =  new SimpleCopier(template, command)
		}

		case class SimpleCatalogue(val toQuery: SimpleLibrary) extends A_Catalogue {
			def query: Seq[SimpleBook] = {
				toQuery books
			}
			def query(predicate: SimpleBook => Boolean): Seq[SimpleBook] = {
				for(book <- toQuery.books if predicate(book))
					yield book
			}
		}


		case class SimpleLibrary(val books: List[SimpleBook]) extends A_Library {
			def place(book: SimpleBook) = new SimpleLibrary(books :+ book)
			def removeBook(book: SimpleBook) = {
				if(books.find(_ == book) == None) error("Unable to find book %s" format book)
				new SimpleLibrary(books filter { _ != book })
			}
			def replace(book: SimpleBook, by: SimpleBook) = removeBook(book).place(by)
			def catalogue = new SimpleCatalogue(this)

			override def toString = "Library with Books %s" format(books.toString)
		}
}