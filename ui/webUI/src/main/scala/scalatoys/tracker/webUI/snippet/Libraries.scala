package scalatoys.tracker.webUI.snippet

import net.liftweb.util.Props
import scalatoys.tracker._
import scalatoys.tracker.impl.mongodb._

class Libraries {

  import com.mongodb._

	val host = "localhost"
	val dbname = "test"

	val scope = new MongoTracker(host, dbname)

  import scope._

  val me = "webUser"
	val libName = "webLib"



  def listBooks = <span> {
    val library = Library(libName)
    val books = library.get.catalogue.query

    books.map{b =>
      <br/> + "Pages: " + b.pages + " FrontPage: " + b.frontPage

    }.reduceRight(_ + <br/> + _ )
  }  </span>
}

