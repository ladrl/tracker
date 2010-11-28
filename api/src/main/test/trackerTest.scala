package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import scalatoys.tracker.impl.{simple => simple}
import scalatoys.tracker.impl.DefaultFactory._
import scalatoys.tracker.impl._
import simple.SimpleFactory._
import simple._

class TrackerTest extends WordSpec with MustMatchers {

	val me = "me"
	import SimpleFactory._
	implicit val factory = SimpleFactory
	
	"A copier" must {
		val copier: Copier = Copier
		"create a book" in {
			copier.from(EmptyBook).by({ x => x }) must be (Book(Nil, Map()))
		}
		
		"put a page in a book it" in {
			val page = Page("text", me)
			copier.from(EmptyBook).by { _ write page } must be (Book(page :: Nil, Map()))
		}
			
		"put a headline on a book" in {
			val headline = HeadLine("line", me)
			copier from EmptyBook by { _ write "Title" -> headline } must be (Book(Nil, Map("Title" -> headline)))
		}
		
		"remove a headline from a book" in {
			val book = Book(Nil, Map("Title" -> HeadLine("text", me)))	
			copier from book by { _ erase "Title" } must be (Book(Nil, Map()))
		}
		"leave a book untouched when copying from it" in {
			val page = Page("First Page", me)
			val book = copier from EmptyBook by { _ write page }
			val secondBook = copier from book by { _ write Page("Second Page", me) }
			book.pages must be (List(page))
		}
	}
	
	"A library" must {
		val copier = Copier
		val me = "me"
		
		"accept a book" in {
			val book = copier from EmptyBook by{ x => x }
			val library = Library(Nil)
			val newLibrary = library.place(book)
			newLibrary must be (Library(book :: Nil))
		}
		"accept two adjacent versions of the same book" in { 
			val book = copier from EmptyBook by { x => x }
			val library = Library(book :: Nil)
			val newBook = copier from book by { _ write "Title" -> HeadLine("text", me)}
			library place newBook must be(Library(book :: newBook :: Nil))
		}
		"allow access to the exact same set of books after a modified library is created" in {
			/* - create a book, put it into a lib 
			   - modify the book (perhaps destructive) and create a new lib with it
			   - check the old version of the lib if the initial book is still in it */
			val book = copier from EmptyBook by { _ write "Title" -> HeadLine("Book 1", me) }
			val initialLibrary = Library(Nil)
			val library = initialLibrary place book
			val secondBook = copier from EmptyBook by { _ write "Title" -> HeadLine("Book 2", me)}
			val newLibrary = library place secondBook
			
			library.catalogue.query{ _ == book } must be (book :: Nil)
		}
	}
	
	"A catalog" must {
		val copier = Copier
		val library = Library(Nil)
		val me = "me"
		"allow to find a list of books by content HeadLine" in { 
			val book1 = copier from EmptyBook by { _ write "Title" -> HeadLine("The Lord of the Rings", me) }
			val book2 = copier from EmptyBook by { _ write "Title" -> HeadLine("The Hobbit", me) }
			val book3 = copier from EmptyBook by { _ write "Title" -> HeadLine("Hitchiker's Guide", me) }
			val filledLibrary = library place book1 place book2 place book3
			val catalogue = filledLibrary.catalogue
			(catalogue query { 
					b => (for(hl <- b.frontPage.get("Title") if hl.toString.contains("The")) yield true) getOrElse false
				}) must be (book1 :: book2 :: Nil)
		}
		"allow to find a list of books by content of Page" in {
			
			(pending)
		}
		"allow to find a list of books by User" in {
			(pending)
		}
		"allow to find the list of a book and it`s predecessors" in {
			(pending)
		}
	}
}