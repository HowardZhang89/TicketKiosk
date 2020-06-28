package messages

import java.text.SimpleDateFormat
import java.util.Calendar

class Event(val name: String, val date: String)
class Ticket(val ticketID: BigInt, val event: Event)
class Receipt(val confirmation: String, val ticket: Ticket)

case object Start
case object BuyTicket
case class ReadTicket(key: BigInt)

case class Success(confirmation: BigInt, ticket: Ticket)
case object Failed

case class MasterToken(numTickets: Int)
case class ExchangeToken(status: Array[Int], numTickets: Int)
