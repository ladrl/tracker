package scalatoys.tracker

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers


case class DuplicateBook(
		book: Book, 
		additionalPages: List[Page] = Nil, 
		additionalHeadlines: Map[String, HeadLine] = Map(), 
		subtractionalHeadlines: List[String] = Nil) extends CopierCommand {
	def write(page: Page) = copy(additionalPages = additionalPages ++ (page :: Nil))
	def write(lines: Map[String, HeadLine]) = copy(additionalHeadlines =  additionalHeadlines/*+ lines  FIXME: create a union from two maps */)
	def erase(lines: List[String]) = copy(subtractionalHeadlines = subtractionalHeadlines ++ lines)
	
	def asNewBook: Book = {
		new Book(
			book.pages ++ additionalPages,
			(book.frontPage /*union additionalHeadlines FIXME: dito */).filterKeys { !subtractionalHeadlines.contains(_) }
		)
	}
}
class CreateBook extends CopierCommand {
	def write(page: Page) = DuplicateBook(asNewBook)
	def write(lines: Map[String, HeadLine]) = DuplicateBook(asNewBook)
	def erase(lines: List[String]) = DuplicateBook(asNewBook)
	
	def asNewBook = new Book(Nil, Map())
}

class TrackerTest extends WordSpec with MustMatchers {
	
	"A copier" must {

		
		val copier = new Copier {
			val command = new CreateBook
			def from[T <% Book](template: T): CopierCommand = {
				new DuplicateBook(template)
			}
		}
		"create a book" in {
			(pending)
		}
		
		"copy a book and put a page in it" in {
			(pending)
		}
	}
}