package scalatoys.tracker.ui

import scalatoys.tracker._
import scalatoys.richjline._
import jline.ConsoleReader

import scalatoys.tracker.impl.simple.Simple

object TrackerCLI {
	val scope = new Simple
	import scope._
	
	def print(book: Book) {
		val frontPage = for((key, value) <- book.frontPage) 
			yield "%s:\t%s" format(key, value.content)
		println(frontPage.foldRight("") { _ + "\n" + _ })
		println("----")
		val pages = for(page <- book.pages)
			yield "By %s: %s" format(page.createdBy, page.content)
		println(pages.foldRight ("") { _ + "\n" + _ })
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
				
				case "new book" => currentBook = Some(EmptyBook)
				
				case "place" => currentLibrary = currentBook match {
					case Some(book) => currentLibrary place book
					case None => currentLibrary
				} 
				
				case writePage(content) => currentBook = for(book <- currentBook) yield Copier from book by { _ write Page(content, "me", new java.util.Date) }
						
				case writeTitle(key, content) =>
					for(book <- currentBook)
						currentBook = Some(Copier from book by { _ write key -> HeadLine(content, "me", new java.util.Date) } )
				
				case "quit" =>
				
				case x => println("Huh? How should i '%s'?" format x)
			}
		}
		println("Bye")
	}
}
