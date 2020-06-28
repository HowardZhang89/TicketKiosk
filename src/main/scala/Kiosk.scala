package kiosk 

import akka.actor.{Actor, ActorRef, ActorSystem, ActorLogging, Props}
import akka.routing.ConsistentHashingRouter
import akka.routing.ConsistentHashingRouter.ConsistentHashMapping
import scala.concurrent.duration._
import scala.io.Source   // for reading files
import java.io._     // for writing files
import java.io.PrintWriter
 
import messages._
import client._

/**
 * KVStore is a local key-value store based on actors.  Each store actor controls a portion of
 * the key space and maintains a hash of values for the keys in its portion.  The keys are 128 bits
 * (BigInt), and the values are of type Any.
 */
case class initKiosks(kioskList: IndexedSeq[ActorRef])

class Kiosk (kioskID: Int, chunkSize: Int) extends Actor {
  
  private var nextKiosk: ActorRef = null;                  // clockwise neighbor
  private var kioskList: IndexedSeq[ActorRef] = null;      // list of all kiosks in case neighbor goes down
  private var ticketsAvailable: Int = 0;
  private var ticketsSold: Int = 0;                        // for issuing ticket IDs
  private val logFile = new PrintWriter(new File("Kiosk" + kioskID + ".txt" ))
  //private var unsoldTickets = new scala.collection.mutable.HashMap[BigInt, Int] // remaining tickets for reach event
  private val soldTickets = new scala.collection.mutable.HashMap[BigInt, Any]  // Hashmap of confirmation numbers to tickets

  override def receive = {
    case initKiosks(kioskList) =>
      setNextKiosk(kioskList)
    case BuyTicket =>
      sender ! processTicketRequest()
    case MasterToken(tokenTickets) =>
      processMasterToken(tokenTickets)
    case ExchangeToken(status, tokenTickets) =>
      processExchangeToken(status, tokenTickets)
  }
  
  private def setNextKiosk(kioskList: IndexedSeq[ActorRef]){
    if(this.kioskID == kioskList.length-1) 
        this.nextKiosk = kioskList(0)
      else{
        this.nextKiosk = kioskList(this.kioskID+1)
      }
  }

  private def issueTicket(): Any = {
    println(self.path.name + " sold a ticket")
    val file = new File("Kiosk"+kioskID+".txt")
    val bw = new BufferedWriter(new FileWriter(file, true))
    val ticketID = kioskID * 10000 + ticketsSold
    bw.write("Ticket id: %05d \n".format(ticketID))
    bw.close()
    this.ticketsAvailable -= 1
    this.ticketsSold += 1
    val newTicket = new Ticket(ticketID, new Event("2Cellos", "Tomorrow"))
    return newTicket
  }

  private def processTicketRequest() : Any = {
    if(this.ticketsAvailable > 0){
        return issueTicket()
      }
      else{
        println(self.path.name + " Not enough tickets.")
        return null;
      }
  }

  private def processMasterToken(tokenTickets: Int){
    var ticketsTaken : Int = 0
      // Situation 1: Kiosk Empty / Token has tickets (Take tickets)
      if (this.ticketsAvailable == 0 && tokenTickets > 0){
        // take one chunk if available
        if(tokenTickets >= this.chunkSize){
          this.ticketsAvailable += this.chunkSize
          ticketsTaken = chunkSize
          println(self.path.name + " took " + chunkSize + " tickets.")
        }
        // take remaining tickets if less than one chunk available
        else{ // ticketsAvailable < chunkSize
          this.ticketsAvailable += tokenTickets
          ticketsTaken = tokenTickets
          println(self.path.name + " took " + ticketsTaken + " tickets.")
        }
        this.nextKiosk ! MasterToken(tokenTickets - ticketsTaken)
      }
      //Situation 2: Token empty / Kiosk has tickets (Donate Tickets)
      else if(tokenTickets == 0 && this.ticketsAvailable > 0){
        // donate tickets
        nextKiosk ! MasterToken(this.ticketsAvailable)
        this.ticketsAvailable = 0;
      }
      // Situation 3: Kiosk has tickets and token has tickets
      else if (this.ticketsAvailable > 0 && tokenTickets > 0){
        //println("No tickets taken. " + tokenTickets + " left.")
        nextKiosk ! MasterToken(tokenTickets)  // pass message on
      }
      // Situation 4: Kiosk and token both have no tickets
      else if(this.ticketsAvailable == 0 && tokenTickets == 0){
        println("All sold out")
      }
      // Something went wrong
      else{
        println("Error? Most likely negative ticket number.")
      }
  }

  private def processExchangeToken(status: Array[Int], tokenTickets: Int){
      if (this.ticketsAvailable < 20 && tokenTickets > 20){
        val remaining: Int = tokenTickets - this.ticketsAvailable
        this.ticketsAvailable += (20 - ticketsAvailable)
        nextKiosk ! ExchangeToken(status, remaining)
      }
      else if(this.ticketsAvailable > 20){
        val remaining = tokenTickets + ticketsAvailable - 20
        this.ticketsAvailable = 20;
        nextKiosk ! ExchangeToken(status, remaining)
      }
      else if(this.ticketsAvailable < 20 && tokenTickets < 20){
        nextKiosk ! ExchangeToken(status, tokenTickets)
      }
      else{
        println("Something Went Wrong.")
      }
  }
}

object TicketKiosk {
  
  def props(id: Int, chunkSize: Int): Props = {
     Props(classOf[Kiosk], id, chunkSize)
  }

}



  

