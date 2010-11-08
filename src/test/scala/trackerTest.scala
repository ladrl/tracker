package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers


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
	}
	
	"A catalog" must {
		"allow to find a list of books by HeadLine" in { 
			(pending)
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