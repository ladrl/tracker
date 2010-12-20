package scalatoys.tracker.ui

import scalatoys.tracker._
import scalatoys.richjline._
import jline.ConsoleReader

class TrackerCLI(val scope: Tracker) {
	import scope._
	
	def printable(book: Book): String = {
		val frontPage = for((key, value) <- book.frontPage) 
			yield "%s:\t%s" format(key, value.content)

		val pages = for(page <- book.pages)
			yield "By %s: %s" format(page.createdBy, page.content)
			
		val frontPageStr = frontPage.foldRight("") { _ + "\n" + _ }
		val pagesStr = pages.foldRight("") { _ + "\n" + _ }
		frontPageStr + "\n-----\n" + pagesStr
	}
	
	def printable(library: Library): String = {
		val header = "Library %s\n--------" format library.name
		
		val body = library.catalogue.query.map{ printable _ }.foldRight("") { _ + "\n\n" + _ }
		header + "\n\n" + body
	}
	
	def run = {
		val reader = new ConsoleReader
		// userInput will return lines until the user types something 'quit' starts with. The user will be prompted by '# '
		val userInput = new UntilIterator(new CLIIterator("# ", reader), { command: String => "quit" startsWith(command.toLowerCase) })
		
		var currentLibrary: Option[Library] = None
		var currentBook: Option[Book] = None
		
		val newLibrary = """new library (.+)""".r
		val openLibrary = """open library (.+)""".r
		val writePage = """write page (.*)""".r
		val writeTitle = """write (.*?) = (.*)""".r
		
		val welcome = """Welcome to tracker.
Written by Lukas Lädrach, Licensed under GPL v2"""
		
		println(welcome)
		
		for(command <- userInput)
		{
			command match {
				case "current library" => println(currentLibrary map { printable _} getOrElse "No current library")
						
				case "current book" => println(currentBook map { printable _ } getOrElse "No current book")
				
				case "new book" => currentBook = Some(EmptyBook)
				case newLibrary(name) => currentLibrary = Some(Library(name, Nil))
				
				case openLibrary(name) => Library(name) match {
					case Some(lib) => currentLibrary = Some(lib)
					case None => println("Unable to open library %s" format name)
				}

				case "place" => (currentLibrary, currentBook) match {
					case (Some(lib), Some(book)) => {
						println("Placing book in library %s" format lib.name)
						currentLibrary = Some(lib place book)
					}
					case (None, None) => println("No current library or book")
					case (None, _) => println("No current library to place")
					case (_, None) => println("No current book to place")
				}
				
				case writePage(content) => currentBook = for(book <- currentBook) yield Copier from book by { _ write Page(content, "me", new java.util.Date) }
						
				case writeTitle(key, content) =>
					for(book <- currentBook)
						currentBook = Some(Copier from book by { _ write key -> HeadLine(content, "me", new java.util.Date) } )
				
				case "quit" => println("Bye")
				
				case x => println("Huh? How should i '%s'?" format x)
			}
		}
	}
}
