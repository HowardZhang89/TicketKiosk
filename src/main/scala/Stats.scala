class Stats {
  var messages: Int = 0
  var bought: Int = 0
  var noTicket: Int = 0   // 
  var reads: Int = 0
  var misses: Int = 0   // tickets available but not sold
  var errors: Int = 0   // the same ticket sold twice
  
  
  var given: Int = 0

  def += (right: Stats): Stats = {
    messages += right.messages
    bought += right.bought
    noTicket += right.noTicket
    reads += right.reads
    misses += right.misses
    errors += right.errors
    this
  }

  override def toString(): String = {
    // the "s" in front of the string is not a typo, don't remove it!
    s"Stats msgs=$messages bought=$bought noTicket=$noTicket reads=$reads miss=$misses err=$errors"
  }
}
