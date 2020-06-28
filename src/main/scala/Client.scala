package client
import akka.actor.{Actor, ActorSystem, ActorRef, Props}
import akka.event.Logging
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

import akka.pattern.ask
import akka.util.Timeout
import messages._

class AnyMap extends scala.collection.mutable.HashMap[BigInt, Any]

/**
 * KVClient implements a client's interface to a KVStore, with an optional writeback cache.
 * Instantiate one KVClient for each actor that is a client of the KVStore.  The values placed
 * in the store are of type Any: it is up to the client app to cast to/from the app's value types.
 * @param stores ActorRefs for the KVStore actors to use as storage servers.
 */

class TicketClient (clientID: Int, stores: Seq[ActorRef]) extends Actor {
  private val purchasedTickets = new AnyMap

  override def receive = {
    case BuyTicket =>
        requestTicket
    case Success(confirmation: BigInt, ticket: Ticket) =>
        purchasedTickets.put(confirmation, ticket)    // add ticket and confirmation number to hashmap
    case Failed =>
        println("Ticket purchase failed.")
  }

  private def requestTicket() {
    // send a BuyTicket message to a kiosk based on clientID Hash
    // We will receive either Success or Failed 
     route(clientID) ! BuyTicket
  }

  /**
    * @param key A key
    * @return An ActorRef for a store server that stores the key's value.
    */
  private def route(key: BigInt): ActorRef = {
    stores((key % stores.length).toInt)
  }
}

object TicketClient{
   def props(clientID: Int, servers: Seq[ActorRef]): Props = {
      Props(classOf[TicketClient], clientID, servers)
   }
}