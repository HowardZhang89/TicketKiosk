import akka.actor.{Actor, ActorRef, ActorSystem, ActorLogging, Props}
import client._
import kiosk._
import messages._

object LoadMaster {

  def main(args : Array[String]) : Unit = {

    val numKiosks = 10
    val system = ActorSystem("system")
    val store = for (i <- 0 to numKiosks) 
      yield system.actorOf(Props(classOf[Kiosk], i, 100), name = "kiosk" + i)
    
    // set the neighbors of each server in the store
    for(server <- store) {
      server ! initKiosks(store)
    }

    // create 10 clients
    val clients = for(i <- 0 to numKiosks)
      yield system.actorOf(Props(classOf[TicketClient], i, store), name = "client" + i)

    store(0) ! MasterToken(10000)
    Thread.sleep(100)
    for(i <- 1 to 1000){
        clients(0) ! BuyTicket
        clients(1) ! BuyTicket
        clients(2) ! BuyTicket
        clients(3) ! BuyTicket
        clients(4) ! BuyTicket
        clients(5) ! BuyTicket
        clients(6) ! BuyTicket
        clients(7) ! BuyTicket  
        clients(8) ! BuyTicket
        clients(9) ! BuyTicket
        clients(10) ! BuyTicket
    }
  

    system.terminate
    }
}