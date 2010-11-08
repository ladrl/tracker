// Tracker
package scalatoys.tracker

import java.util.Date

/*
	This is a library.
	Books can be created and put in it.
	Books are made from a front page with head lines and a number of pages inside.
	The catalogue can be used to find a particular book.
	Everything is undestroyable, one can only create modified copies but nothing can be changed.
	Someone has to do stuff.
*/

case class User(val name: String)

trait Page {
	// Fixme: Content should be flexible!
	def content: String
	def createdBy: User
	def createdAt: Date
}

trait HeadLine {
	// Fixme: Content should be flexible!
	def content: String
	def createdBy: User
	def createdAt: Date
}

trait Book {
	def pages: List[Page]
	def frontPage: Map[String, HeadLine]
}


// val copy = new Copier ...
// val book = someBook
// val newBook = copy from someBook by { _ write new Page write "Title" -> "New Title" } 
trait Copier {
	protected def book: Book
	protected def command: Option[CopierCommand]
	
	def from(template: Book): Copier
	
	def by(doing: CopierCommand => CopierCommand): Book = doing(command get).asNewBook
}

trait CopierCommand {
	// Book.write must be a Monad because the following line should only create _one_ new instance (when calling asNewBook)
	
	// Metaphor: To do this, you put a book on the copier, then instruct the copier to add a page and to add a line, then press 'copy' which creates the new book
	// copier from book write page write page2 write "Title" -> "Mein Tagebuch" asNewBook
	def write(page: Page): CopierCommand
	def write(line: (String, HeadLine)): CopierCommand
	def write(lines: Map[String, HeadLine]): CopierCommand
	def erase(line: String): CopierCommand
	def erase(lines: List[String]): CopierCommand
	
	def asNewBook: Book
}

trait Library {
	def place(book: Book): Library
	def replaceBook(replace: Book, by: Book): Library = removeBook(replace).place(by)
	def removeBook(book: Book): Library
	def catalogue: Catalogue
}

trait Catalogue
{
	def query(predicate: Book => Boolean): List[Book]
	def query[Q](query: Q): Seq[Book] = { Nil }
}

// Immutable Library, Book & Page
// Kopiermaschine à la Tim & Struppi


// State is an idea _building_ on the base of the library metaphor and is implemented elsewhere




