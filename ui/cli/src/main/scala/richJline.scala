package scalatoys.richjline


import jline._

class CLIIterator(prompt: String, reader: ConsoleReader) extends Iterator[String] {
	def next = reader.readLine(prompt)
	def hasNext = true
}

class UntilIterator[T](i: Iterator[T], until: T => Boolean) extends Iterator[T] 
{ 
	var found = false
	def next = { val element = i.next; if (until(element)) found = true; element }
	def hasNext = !found
}