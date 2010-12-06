package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers


trait TrackerTest extends WordSpec with MustMatchers {
	val scope: Tracker
	
	val me = "me"
	def test = {
		"The tracker" must {
			"allow the creation of a page" in {
				val p = scope.Page("text", "user", new java.util.Date)
			}
			"allow the creation of a book" in {
				val p = scope.Page("text", "user", new java.util.Date)
				val fp = Map("title" -> scope.HeadLine("titletext", "user", new java.util.Date))
				val book = scope.Book(p :: Nil, fp)
			}
		}
		"A copier" must {
			val copier = scope.Copier()
			"create a book" in {
				copier.by({ x => x }) must be (scope.Book(Nil, Map()))
			}
		
			"put a page in a book it" in {
				val page = scope.Page("text", me, new java.util.Date)
				copier.by { _ write page } must be (scope.Book(page :: Nil, Map()))
			}
			
			"put a headline on a book" in {
				val headline = scope.HeadLine("line", me, new java.util.Date)
				copier from scope.EmptyBook by { _ write "Title" -> headline } must be (scope.Book(Nil, Map("Title" -> headline)))
			}
		
			"remove a headline from a book" in {
				val book = scope.Book(Nil, Map("Title" -> scope.HeadLine("text", me, new java.util.Date)))	
				copier from book by { _ erase "Title" } must be (scope.Book(Nil, Map()))
			}
			"leave a book untouched when copying from it" in {
				val page = scope.Page("First Page", me, new java.util.Date)
				val book = copier from scope.EmptyBook by { _ write page }
				val secondBook = copier from book by { _ write scope.Page("Second Page", me, new java.util.Date) }
				book.pages must be (List(page))
			}
		}
		"A library" must {
			val copier = scope.Copier()
			val me = "me"
		
			"accept a book" in {
				val book = copier from scope.EmptyBook by{ x => x }
				val library = scope.Library(Nil)
				val newLibrary = library.place(book)
				newLibrary must be (scope.Library(book :: Nil))
			}
			"accept two adjacent versions of the same book" in { 
				val book = copier from scope.EmptyBook by { x => x }
				val library = scope.Library(book :: Nil)
				val newBook = copier from book by { _ write "Title" -> scope.HeadLine("text", me, new java.util.Date)}
				library place newBook must be(scope.Library(book :: newBook :: Nil))
			}
			"allow access to the exact same set of books after a modified library is created" in {
				// - create a book, put it into a lib 
				// - modify the book (perhaps destructive) and create a new lib with it
				// - check the old version of the lib if the initial book is still in it
				val book = copier from scope.EmptyBook by { _ write "Title" -> scope.HeadLine("Book 1", me, new java.util.Date) }
				val initialLibrary = scope.Library(Nil)
				val library = initialLibrary place book
				val secondBook = copier from scope.EmptyBook by { _ write "Title" -> scope.HeadLine("Book 2", me, new java.util.Date)}
				val newLibrary = library place secondBook
			
				library.catalogue.query{ _ == book } must be (book :: Nil)
			}
		}
		/*
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
*/
	}
}
