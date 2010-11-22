package scalatoys.tracker.ui

import scalatoys.tracker._
import scalatoys.richjline._
import jline.ConsoleReader

import scalatoys.tracker.impl.{simple => simple}
import scalatoys.tracker.impl.DefaultFactory._
import scalatoys.tracker.impl._

object TrackerCLI {
	import simple._
	implicit val factory = SimpleFactory
	
	
	def main(args: Array[String]) = {
		val reader = new ConsoleReader
		// userInput will return lines until the user types something 'quit' starts with. The user will be prompted by '# '
		val userInput = new UntilIterator(new CLIIterator("# ", reader), { command: String => "quit" startsWith(command.toLowerCase) })
		
		var currentLibrary:Library = Library(Nil)
		var currentBook: Option[Book] = None
		
		val writePage = """write page (.*)""".r
		val writeTitle = """write (.*?) = (.*)""".r
		for(command <- userInput)
		{
			command match {
				case "books" =>
					for(book <- currentLibrary.catalogue.query)
						println(book)
						
				case "current book" => println("Current book: %s" format currentBook)
				
				case "new book" => currentBook = Some(Book(Nil, Map()))
				
				case "place" =>
					for(book <- currentBook) 
						currentLibrary = currentLibrary place book
				
				case writePage(content) => 
					for(book <- currentBook) 
						currentBook = Some(Copier from book by { _ write Page(content, "me") })
						
				case writeTitle(key, content) => for(book <- currentBook) currentBook = Some(Copier from book by { _ write key -> HeadLine(content, "me") } )
				
				case x => println("Huh? How should i '%s'?" format x)
			}
		}
		println("Bye")
	}
}
