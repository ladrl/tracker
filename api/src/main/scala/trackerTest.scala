package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers


trait TrackerTest extends WordSpec with MustMatchers {
	val scope: Tracker
	
	val me = "me"
	val libName = "testLib"
	def test = {
		import scope._ // IMPORTANT: scope _must_ be 'val' so it can serve as 'stable type'
		"The tracker" must {
			"allow the creation of a page" in {
				val p: Page = Page("text", "user", new java.util.Date)
			}
			"allow the creation of a book" in {
				val p: scope.Page = scope.Page("text", "user", new java.util.Date)
				val fp: Map[String, scope.HeadLine] = Map("title" -> scope.HeadLine("titletext", "user", new java.util.Date))
				val book = scope.Book(p :: Nil, fp)
			}
		}
		"A copier" must {
			val copier = Copier()
			"create a book" in {
				copier.by({ x => x }) must be (Book(Nil, Map()))
			}
		
			"put a page in a book" in {
				val page = Page("text", me, new java.util.Date)
				copier.by { _ write page } must be (Book(page :: Nil, Map()))
			}
			
			"put two pages in a book" in {
				val page1 = Page("text1", me, new java.util.Date)
				val page2 = Page("text2", me, new java.util.Date)
				val book1 = copier from EmptyBook by { _ write page1 }
				val book2 = copier from book1 by { _ write page2 }
				book1 must be (Book(page1 :: Nil, Map()))
				book2 must be (Book(page1 :: page2 :: Nil, Map()))
				
			}
			
			"put a headline on a book" in {
				val headline = HeadLine("line", me, new java.util.Date)
				copier from EmptyBook by { _ write "Title" -> headline } must be (Book(Nil, Map("Title" -> headline)))
			}
		
			"remove a headline from a book" in {
				val book = Book(Nil, Map("Title" -> HeadLine("text", me, new java.util.Date)))	
				copier from book by { _ erase "Title" } must be (Book(Nil, Map()))
			}
			"leave a book untouched when copying from it" in {
				val page = Page("First Page", me, new java.util.Date)
				val book = copier from EmptyBook by { _ write page }
				val secondBook = copier from book by { _ write Page("Second Page", me, new java.util.Date) }
				book.pages must be (List(page))
			}
		}
		"A library" must {
			val copier = Copier()
			val me = "me"
		
			"accept a book" in {
				val book = copier from EmptyBook by{ x => x }
				val library = Library(libName, Nil)
				val newLibrary = library.place(book)
				newLibrary.catalogue.query must be (book :: Nil)
			}
			"accept two adjacent versions of the same book" in { 
				val book = copier from EmptyBook by { x => x }
				val library = Library(libName, book :: Nil)
				val newBook = copier from book by { _ write "Title" -> HeadLine("text", me, new java.util.Date)}
				val newLibrary = library place newBook 
				newLibrary.catalogue.query must be (book :: newBook :: Nil)
			}
			"allow access to the exact same set of books after a modified library is created" in {
				// - create a book, put it into a lib 
				// - modify the book (perhaps destructive) and create a new lib with it
				// - check the old version of the lib if the initial book is still in it
				val book = copier from EmptyBook by { _ write "Title" -> HeadLine("Book 1", me, new java.util.Date) }
				val initialLibrary = Library(libName, Nil)
				val library = initialLibrary place book
				val secondBook = copier from EmptyBook by { _ write "Title" -> HeadLine("Book 2", me, new java.util.Date)}
				val newLibrary = library place secondBook
			
				library.catalogue.query{ _ == book } must be (book :: Nil)
			}
		}

		"A catalog" must {
			val copier = Copier
			val library = Library(libName, Nil)
			val me = "me"
			"allow to find a list of books by content HeadLine" in { 
				val book1 = copier from EmptyBook by { _ write "Title" -> HeadLine("The Lord of the Rings", me, new java.util.Date) }
				val book2 = copier from EmptyBook by { _ write "Title" -> HeadLine("The Hobbit", me, new java.util.Date) }
				val book3 = copier from EmptyBook by { _ write "Title" -> HeadLine("Hitchiker's Guide", me, new java.util.Date) }
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
}
