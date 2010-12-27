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
abstract class Tracker {
	type Page     <: A_Page
	type HeadLine <: A_HeadLine
	type Book     <: A_Book
	type Library  <: A_Library
	type Copier   <: A_Copier
	type Command  <: A_Command
	type Catalogue<: A_Catalogue
	
	trait Content {
		def content: String
		def createdBy: String
		def createdAt: Date
	}

	trait A_Page extends Content

	trait A_HeadLine extends Content

	trait A_Book {
		def pages: Seq[Page]
		def frontPage: Map[String, HeadLine]
	}

	trait A_Copier {
		def from(template: Book): Copier
	
		def command: Command
		def by(doing: Command => Command): Book = doing(command ).asNewBook
	}

	trait A_Library {
		val name: String
		def place(book: Book): Library
		def replaceBook(replace: Book, by: Book) = removeBook(replace).place(by)
		def removeBook(book: Book): Library
		def catalogue: Catalogue
	}

	trait A_Command {
		// Book.write must be a Monad because the following line should only create _one_ new instance (when calling asNewBook)
	
		// Metaphor: To do this, you put a book on the copier, then instruct the copier to add a page and to add a line, then press 'copy' which creates the new book
		// copier from book write page write page2 write "Title" -> "Mein Tagebuch" asNewBook
		def write(pages: Seq[Page]): Command
		def write(lines: Map[String, HeadLine]): Command
		def erase(lines: Seq[String]): Command
	
		def write(line: (String, HeadLine)): Command = write(Map( line._1 -> line._2))
		def write(page: Page): Command = write(List(page))
		def erase(line: String): Command = erase(List(line))
	
		def asNewBook: Book
	}

	trait A_Catalogue
	{
		def query: Seq[Book]
		def query(predicate: Book => Boolean): Seq[Book]
		def predecessors(of: Book) : Seq[Book]
	}


	def Page(content: String, createdBy: String, createdAt: Date): Page 
	def HeadLine(content: String, createdBy: String, createdAt: Date): HeadLine
	def Book(pages: Seq[Page], frontPage: Map[String, HeadLine]): Book
	def Library(name: String, content: Seq[Book]): Library
	def Library(name: String): Option[Library]
	def Copier(): Copier
	def EmptyBook: Book
}