package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers


// TODO: Add factory object/class which abstracts the concrete instanciation of objects (Simple*)

class TrackerTest extends WordSpec with MustMatchers {
	import impl.simple._
	"A copier" must {
		implicit val me = User("me")
			
		val copier: Copier = new SimpleCopier
		"create a book" in {
			copier.from(EmptyBook).by({ x => x }) must be (SimpleBook(Nil, Map()))
		}
		
		"put a page in a book it" in {
			copier.from(EmptyBook).by { _ write SimplePage("text", me) } must be (SimpleBook(SimplePage("text", me) :: Nil, Map()))
		}
			
		"put a headline on a book" in {
			copier from EmptyBook by { _ write "Title" -> SimpleHeadLine("line", me) } must be (SimpleBook(Nil, Map("Title" -> SimpleHeadLine("line", me))))
		}
		
		"remove a headline from a book" in {
			val book = SimpleBook(Nil, Map("Title" -> SimpleHeadLine("text", me)))	
			copier from book by { _ erase "Title" } must be (SimpleBook(Nil, Map()))
		}
		"leave a book untouched when copying from it" in {
			val page = SimplePage("First Page", me)
			val book = copier from EmptyBook by { _ write page }
			val secondBook = copier from book by { _ write SimplePage("Second Page", me) }
			book.pages must be (List(page))
		}
	}
	
	"A library" must {
		val copier = new SimpleCopier
		"accept a book" in {
			val book = copier from EmptyBook by{ x => x }
			val library = new SimpleLibrary(Nil)
			val newLibrary = library.place(book)
			newLibrary must be (new SimpleLibrary(book :: Nil))
		}
		"accept two adjacent versions of the same book" in { 
			val book = copier from EmptyBook by { x => x }
			val library = new SimpleLibrary(book :: Nil)
			val newBook = copier from book by { _ write "Title" -> SimpleHeadLine("text", User("me"))}
			library place newBook must be(new SimpleLibrary(book :: newBook :: Nil))
		}
		"allow access to the exact same set of books after a modified library is created" in {
			/* - create a book, put it into a lib 
			   - modify the book (perhaps destructive) and create a new lib with it
			   - check the old version of the lib if the initial book is still in it */
			(pending)
		}
	}
	
	"A catalog" must {
		val copier = new SimpleCopier
		val library = new SimpleLibrary(Nil)
		"allow to find a list of books by HeadLine" in { 
			implicit val me = User("me")
			val book1 = copier from EmptyBook by { _ write "Title" -> SimpleHeadLine("The Lord of the Rings", me) }
			val book2 = copier from EmptyBook by { _ write "Title" -> SimpleHeadLine("The Hobbit", me) }
			val book3 = copier from EmptyBook by { _ write "Title" -> SimpleHeadLine("Hitchiker's Guide", me) }
			val filledLibrary = library place book1 place book2 place book3
			val catalogue = filledLibrary.catalogue
			(catalogue query { 
					b => (for(hl <- b.frontPage.get("Title") if hl.toString.contains("The")) yield true) getOrElse false
				}) must be (book1 :: book2 :: Nil)
			
		}
		"allow to find a list of books by Page" in {
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