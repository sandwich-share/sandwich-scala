package peer

import java.net.InetAddress
import com.twitter.json.{Json, JsonSerializable}
import java.util.Date
import utils.Utils

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:31 AM
 * To change this template use File | Settings | File Templates.
 */
case class Peer(var ipAddress: InetAddress, var fileHash: Int, var lastSeen: Date) extends JsonSerializable {
  override def toJson(): String = Json.build(Map("IP" -> ipAddress.toString, "IndexHash" -> fileHash.toString, "LastSeen" -> lastSeen.toString)).toString
  override def equals(value: Any) = if(value.isInstanceOf[Peer]) {
    value.asInstanceOf[Peer].ipAddress == ipAddress
  } else {
    false
  }
}

object Peer {
  def apply(map: Map[String, String]) = Peer(InetAddress.getByName(map("IP")), map("IndexHash").toInt, Utils.dateParser.parse(map("LastSeen")))
}