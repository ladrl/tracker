package scalatoys.tracker.impl
	import java.util.Date
	import scalatoys.tracker._
	
	
	object Factory {
		def Page(content: String, createdBy: String, createdAt: Date)(implicit factory: Factory)  = factory.newPage(content, createdBy, createdAt)
		def HeadLine(content: String, createdBy: String, createdAt: Date)(implicit factory: Factory)  = factory.newHeadLine(content, createdBy, createdAt)
		def Book(pages: Seq[Page], frontPage: Map[String, HeadLine])(implicit factory: Factory)  = factory.newBook(pages, frontPage)
		def Library(content: Seq[Book])(implicit factory: Factory)  = factory.newLibrary(content)
		def Copier(implicit factory: Factory) = factory.newCopier()
	}

	object DefaultFactory {
		def Page(content: String, createdBy: String, createdAt: Date = new Date)(implicit factory: Factory)  = factory.newPage(content, createdBy, createdAt)
		def HeadLine(content: String, createdBy: String, createdAt: Date = new Date)(implicit factory: Factory)  = factory.newHeadLine(content, createdBy, createdAt)
		def Book(pages: Seq[Page], frontPage: Map[String, HeadLine])(implicit factory: Factory)  = factory.newBook(pages, frontPage)
		def Library(content: Seq[Book])(implicit factory: Factory)  = factory.newLibrary(content)
		def Copier(implicit factory: Factory) = factory.newCopier()
	}
	
package simple {
	object SimpleFactory extends Factory {
		override def newPage(content: String, createdBy: String, createdAt: Date) = SimplePage(content, createdBy, createdAt)
		override def newHeadLine(content: String, createdBy: String, createdAt: Date) = SimpleHeadLine(content, createdBy, createdAt)
		override def newBook(pages: Seq[SimplePage], frontPage: Map[String, SimpleHeadLine]): SimpleBook = SimpleBook(pages.toList, frontPage)
		override def newLibrary(content: Seq[SimpleBook]) = SimpleLibrary(content.toList)
		override def newCopier() = new SimpleCopier()
	}

	
	case class DuplicateBook(
		book: SimpleBook, 
		additionalPages: List[SimplePage] = Nil, 
		additionalHeadlines: Map[String, SimpleHeadLine] = Map(), 
		subtractionalHeadlines: List[String] = Nil) extends CopierCommand {
			def write(page: SimplePage)                   = copy(additionalPages = additionalPages :+ page)
			def write(line: (String, SimpleHeadLine))     = copy(additionalHeadlines = additionalHeadlines + line)
			def write(lines: Map[String, SimpleHeadLine]) = copy(additionalHeadlines =  additionalHeadlines ++ lines)
			def erase(line: String)                       = copy(subtractionalHeadlines = subtractionalHeadlines :+ line)
			def erase(lines: List[String])                = copy(subtractionalHeadlines = subtractionalHeadlines ++ lines)

			def asNewBook: SimpleBook = SimpleBook(
				book.pages ++ additionalPages,
				(book.frontPage ++ additionalHeadlines).filterKeys { !subtractionalHeadlines.contains(_) }
				)
			}
			
	case class SimpleBook(val pages: Seq[SimplePage], val frontPage: Map[String, SimpleHeadLine]) extends Book {
		type P = SimplePage
		type HL = SimpleHeadLine
	}

	case class SimpleHeadLine(val content: String, val createdBy: String, val createdAt: Date = new java.util.Date) extends HeadLine

	case class SimplePage(val content: String, val createdBy: String,  val createdAt: Date = new java.util.Date) extends Page

	object SimpleEmptyBook extends SimpleBook(Nil, Map[String, SimpleHeadLine]())

	class SimpleCopier(val book: SimpleBook = SimpleEmptyBook, val command: Option[CopierCommand] = None) extends Copier {
		def from(template: SimpleBook) = new SimpleCopier(template, Some(DuplicateBook(template)))
	}
	
	case class SimpleCatalogue(val toQuery: SimpleLibrary) extends Catalogue {
		def query: Seq[Book] = {
			toQuery books
		}
		def query(predicate: Book => Boolean): Seq[Book] = {
			for(book <- toQuery.books if predicate(book))
				yield book
		}
	}
	
	case class SimpleLibrary(val books: List[SimpleBook]) extends Library {
		def place(book: Book) = SimpleFactory.newLibrary(books :+ book)
		def removeBook(book: Book) = {
			if(books.find(_ == book) == None) error("Unable to find book %s" format book)
			SimpleFactory.newLibrary(books filter { _ != book })
		}
		def replace(book: Book, by: Book) = removeBook(book).place(by)
		def catalogue: Catalogue = new SimpleCatalogue(this)

		override def toString = "Library with Books %s" format(books.toString)
	}
}