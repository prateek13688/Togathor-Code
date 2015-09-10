

/**
 * @author PRATEEK

 */
package individualPackage

import java.security.MessageDigest
import collection.mutable.ListBuffer
object example {
  def main(args: Array[String]){
    var bitCoins: ListBuffer[Tuple2[String, String]] = digestSHA256("kjsdfk11", 1, 1, "adobra", 1, 100)
    for(tuples : Tuple2[String, String] <- bitCoins){
         println(tuples._1, tuples._2)
    }
  }
  def digestSHA256(input: String, zeros: Int, actorId: Int, gatorId: String, start: Int, end: Int) : ListBuffer[Tuple2[String, String]] = {
    var hasZeroes: String = ""
    var bitCoins = new ListBuffer[Tuple2[String, String]]()
    for (i <- 1 to zeros)
          hasZeroes += "0"
    for(attempts <- start to end){
      val s : String = gatorId +";" +input + actorId.toString() + attempts.toString()
       val digest : String = MessageDigest.getInstance("SHA-256").digest(s.getBytes)
         .foldLeft("")((s: String, b: Byte) => s + Character.forDigit((b & 0xf0) >> 4, 16) +
                                                                 Character.forDigit(b & 0x0f, 16))
    if(digest.startsWith(hasZeroes))
      bitCoins += ((s, digest))
    }
    return bitCoins
  }
}