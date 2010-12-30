package scalatoys.tracker.webUI.snippet

import net.liftweb.util.Props
import scalatoys.tracker._
import scalatoys.tracker.impl.mongodb._

import net.liftweb.common.{Full,Box,Empty,Failure,Loggable}
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,StatefulSnippet,SHtml}
import Box._


import scala.xml.NodeSeq

class Libraries {

  import com.mongodb._

	val host = "localhost"
	val dbname = "test"

	val scope = new MongoTracker(host, dbname)

  import scope._

  val me = "webUser"
	val libName = "webLib"

  def listBooks(xhtml: NodeSeq): NodeSeq = {
	val library = Library(libName) ?~! "Library %s not found".format(libName)
	(for(l <- library)
		yield showBooks(l, xhtml)).get
  }

  def showBooks(library: Library, xhtml: NodeSeq) = library.catalogue.query.flatMap { showBook(_, xhtml) }

  def showBook(book: Book, xhtml: NodeSeq): NodeSeq = {
	bind("book", xhtml, 
		"frontPage" -> showFrontPage(book) _,
		"pages" -> book.pages.flatMap { p =>
			println(chooseTemplate("book", "pages", xhtml))
			bind("page", chooseTemplate("book", "pages", xhtml), "page" -> p.content)
		}.toSeq
	)
  }

  def showFrontPage(book: Book)(xhtml: NodeSeq): NodeSeq = {
    val res = book.frontPage.flatMap{ t => showHeadLine(t._1, t._2)(xhtml) }.toList

	res
  }

  def showHeadLine(key: String, hl: HeadLine)(xhtml: NodeSeq) = {
    bind("page", xhtml, "key" -> key, "value" -> hl.content)
  }
}

