package example.travel {
package model {
  
  import net.liftweb.common.{Full,Box,Empty,Failure}
  import net.liftweb.mapper._
  import net.liftweb.sitemap.Loc._
  import scala.xml.{NodeSeq,Node}
  
  object Customer extends Customer 
      with KeyedMetaMapper[Long, Customer]
      with MetaMegaProtoUser[Customer]{
    
    override def dbTableName = "customers"
    override def fieldOrder = id :: firstName :: lastName :: 
      email :: password :: Nil
    
    // proto user
    override val basePath = "account" :: Nil
    override def homePage = "/"
    override def skipEmailValidation = true
    override def loginMenuLocParams = LocGroup("public") :: super.loginMenuLocParams
    override def createUserMenuLocParams = LocGroup("public") :: super.createUserMenuLocParams
    override def screenWrap: Box[Node] = 
      Full(
        <lift:surround with="default" at="content">
          <div id="box1" class="topbg">
            <lift:bind />
          </div>
          <lift:with-param name="sidebar">
            <lift:embed what="_light_basket" />
          </lift:with-param>
        </lift:surround>
      )
   
  }
  class Customer extends MegaProtoUser[Customer] with CreatedUpdated {
    def getSingleton = Customer
    
    def participatingIn: List[Long] = 
      Bid.findAll(By(Bid.customer, this.id)).map(_.auction.obj)
        .distinct.filter(!_.isEmpty).map(_.open_!.id.is)
  }
  
}}