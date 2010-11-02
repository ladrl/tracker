package scalatoys.tracker.ui

import scalatoys.tracker._
//import org.apache.commons.cli._

object TrackerCLI {
    def formatEntry(entry: Entry) = {
        val header = "%s - %s" format (entry name, entry.state.name)
        
        val content = entry.content.foldLeft(""){ _ + "\n" + _ }
        
        "%s:\n%s" format (header, content)
    }
    
    def main(args: Array[String]) {
        // look at the first argument 
        try {
            val tracker = Tracker.create("DB Test.trk")
            if(args.size < 1)
                error("needs args")
            
            args(0).toLowerCase match {
                case "new" => {
                    tracker create
                }
                case "update" =>
                case unknownCommand => error("Command '%s' is not known" format unknownCommand)
            }
            
            tracker.query{ _ => true }.foreach{ e => println(formatEntry(e)) }
        } catch {
            case e: RuntimeException => println("Error: %s" format e.getMessage)
        }
    }
    
}
