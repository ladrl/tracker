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
	
	def print(book: Book) {
		for((key, value) <- book.frontPage)
			println("%s:\t%s" format(key, value.content))
		println("----")
		for(page <- book.pages)
			println("By %s: %s" format(page.createdBy, page.content))
	}
	
	def print(library: Library) {
		println("-------")
		println("Library")
		println("-------")
		for(book <- library.catalogue.query)
			print(book)
	}
	
	def main(args: Array[String]) = {
		val reader = new ConsoleReader
		// userInput will return lines until the user types something 'quit' starts with. The user will be prompted by '# '
		val userInput = new UntilIterator(new CLIIterator("# ", reader), { command: String => "quit" startsWith(command.toLowerCase) })
		
		var currentLibrary:Library = Library(Nil)
		var currentBook: Option[Book] = None
		
		val writePage = """write page (.*)""".r
		val writeTitle = """write (.*?) = (.*)""".r
		
		val welcome = """Welcome to tracker.
Written by Lukas Lädrach, Licensed under GPL v2"""
		
		println(welcome)
		
		for(command <- userInput)
		{
			command match {
				case "books" => print(currentLibrary)
						
				case "current book" => currentBook match {
					case Some(book) => print(book)
					case None => println("No current book")
				}
				
				case "new book" => currentBook = Some(Book(Nil, Map()))
				
				case "place" =>
					for(book <- currentBook) 
						currentLibrary = currentLibrary place book
				
				case writePage(content) => 
					for(book <- currentBook) 
						currentBook = Some(Copier from book by { _ write Page(content, "me") })
						
				case writeTitle(key, content) =>
					for(book <- currentBook)
						currentBook = Some(Copier from book by { _ write key -> HeadLine(content, "me") } )
				
				case x => println("Huh? How should i '%s'?" format x)
			}
		}
		println("Bye")
	}
}
