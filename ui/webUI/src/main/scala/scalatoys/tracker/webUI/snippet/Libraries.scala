package scalatoys.tracker.webUI.snippet

import net.liftweb.util.Props
import scalatoys.tracker._
import scalatoys.tracker.impl.mongodb._

import net.liftweb.common._
import net.liftweb.common.Box._

class Libraries {

  import com.mongodb._

	val host = "localhost"
	val dbname = "test"

	val scope = new MongoTracker(host, dbname)

  import scope._

  val me = "webUser"
	val libName = "webLib"


  def listBooks = <span> {
    val library = Library(libName) ?~! "Library %s not found".format(libName)
    library.map{_.catalogue.query.map{b =>
      <br/> + "Pages: " + b.pages + " FrontPage: " + b.frontPage

    }.reduceRight(_ + <br/> + _ )}

  }  </span>
}

