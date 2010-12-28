package example.travel {
package model {
  
  import scala.xml.{MetaData,Text,Null,UnprefixedAttribute}
  import net.liftweb.common.{Full,Box,Empty,Failure}
  import net.liftweb.mapper._
  
  object Bid 
    extends Bid 
    with LongKeyedMetaMapper[Bid]{
      override def dbTableName = "bids"
    }

  class Bid extends LongKeyedMapper[Bid] with IdPK with CreatedUpdated {
    def getSingleton = Bid
    // fields
    object amount extends MappedDouble(this)
    
    // relationship
    object customer extends LongMappedMapper(this, Customer){
      override def dbColumnName = "customer_id"
    }
    object auction extends LongMappedMapper(this, Auction){
      override def dbColumnName = "auction_id"
    }
    
    // helpers
    def nextBidValue: Double = amount.is + 1D
  }
  
  
}}
