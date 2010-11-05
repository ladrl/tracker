package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import java.util.Date

case class DuplicateBook(
		book: Book, 
		additionalPages: List[Page] = Nil, 
		additionalHeadlines: Map[String, HeadLine] = Map(), 
		subtractionalHeadlines: List[String] = Nil) extends CopierCommand {
	def write(page: Page) = copy(additionalPages = additionalPages ++ (page :: Nil))
	def write(line: (String, HeadLine)) = copy(additionalHeadlines = additionalHeadlines + line)
	def write(lines: Map[String, HeadLine]) = copy(additionalHeadlines =  additionalHeadlines ++ lines)
	def erase(line: String) = copy(subtractionalHeadlines = subtractionalHeadlines :+ line)
	def erase(lines: List[String]) = copy(subtractionalHeadlines = subtractionalHeadlines ++ lines)
	
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

case class SimpleHeadLine(implicit val createdBy: User, val createdAt: Date = new java.util.Date) extends HeadLine

case class SimplePage(implicit val createdBy: User,  val createdAt: Date = new java.util.Date) extends Page

class SimpleCopier(val book: Book = EmptyBook, val command: Option[CopierCommand] = None) extends Copier {
	def from(template: Book) = new SimpleCopier(template, Some(DuplicateBook(template)))
}

class TrackerTest extends WordSpec with MustMatchers {
	
	"A copier" must {
		implicit val me = User("me")
			
		val copier: Copier = new SimpleCopier
		"create a book" in {
			copier.from(EmptyBook).by({ x => x }) must be (SimpleBook(Nil, Map()))
		}
		
		"put a page in a book it" in {
			copier.from(EmptyBook).by { _ write new SimplePage() } must be (new SimpleBook(new SimplePage :: Nil, Map()))
		}
		
		"put a headline on a book" in {
			copier from EmptyBook by { _ write "Title" -> SimpleHeadLine() } must be (SimpleBook(Nil, Map("Title" -> SimpleHeadLine())))
		}
		
		"remove a headline from a book" in {
			val book = SimpleBook(Nil, Map("Title" -> SimpleHeadLine()))
			copier from book by { _ erase "Title" } must be (SimpleBook(Nil, Map()))
		}
	}
	
	"A library" must {
		"accept a book" in { (pending) }
		"return a book" in { (pending) }
		"accept two adjacent versions of the same book" in { (pending) }
	}
	
	"A catalog" must {
		"allow to find a list of books by HeadLine" in { (pending) }
		"allow to find a list of books by Page" in { (pending) }
		"allow to find a list of books by User" in { (pending) }
		"allow to find the list of a book and it`s predecessors" in { (pending) }
	}
}