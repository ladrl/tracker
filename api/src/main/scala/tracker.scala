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

trait Page {
	// Fixme: Content should be flexible!
	def content: String
	def createdBy: String
	def createdAt: Date
}

trait HeadLine {
	// Fixme: Content should be flexible!
	def content: String
	def createdBy: String
	def createdAt: Date
}

trait Book {
	type P <: Page
	type HL <: HeadLine
	def pages: Seq[P]
	def frontPage: Map[String, HL]
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
	def replaceBook(replace: Book, by: Book) = removeBook(replace).place(by)
	def removeBook(book: Book): Library
	def catalogue: Catalogue
}

trait Catalogue
{
	def query: Seq[Book]
	def query(predicate: Book => Boolean): Seq[Book]
// Todo: Perhaps create generic cataloge taking a any object for query..
//	def query[Q](query: Q): Seq[Book] = { Nil }
}


trait Factory {
	def newPage(content: String, createdBy: String, createdAt: Date): Page 
	def newHeadLine(content: String, createdBy: String, createdAt: Date): HeadLine
	def newBook(pages: Seq[Page], frontPage: Map[String, HeadLine]): Book
	def newLibrary(content: Seq[Book]): Library
	def newCopier(): Copier
}

object EmptyBook extends Book {
	type P = Page
	type HL = HeadLine
	override val pages = Nil
	override val frontPage = Map[String, HL]()
}


// Immutable Library, Book & Page
// Kopiermaschine à la Tim & Struppi


// State is an idea _building_ on the base of the library metaphor and is implemented elsewhere



