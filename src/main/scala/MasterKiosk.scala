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

class MasterKiosk (chunkSize: Int) extends Actor {
  
  private var nextKiosk: ActorRef = null;                  // clockwise neighbor
  private var kioskList: IndexedSeq[ActorRef] = null;      // list of all kiosks in case neighbor goes down
  private var ticketsAvailable: Int = 0;
  private val logFile = new PrintWriter(new File("Master.txt" ))
  //private var unsoldTickets = new scala.collection.mutable.HashMap[BigInt, Int] // remaining tickets for reach event
  private val soldTickets = new scala.collection.mutable.HashMap[BigInt, Any]  // Hashmap of confirmation numbers to tickets

  override def receive = {
    case MasterToken(tokenTickets) =>
      processMasterToken(tokenTickets)
    case ExchangeToken(status, tokenTickets) =>
      processExchangeToken(status, tokenTickets)
  }


  private def processMasterToken(tokenTickets: Int){
    if(tokenTickets > 0)
      nextKiosk  ! MasterToken
    else
      nextKiosk ! ExchangeToken
    }

    private def processExchangeToken(status: Array[Int], tokenTickets: Int){
        var shouldRun = true;
        for(value <- status){
            if(value == 0){
                //break;
            }
            else{
                //System.terminate
            }
        }
    }
}


object MasterKiosk {
  
  def props(id: Int, chunkSize: Int): Props = {
     Props(classOf[MasterKiosk], id, chunkSize)
  }

}

