package sandwich.utils

import java.net.InetAddress
import sandwich.client.peer.Peer
import java.io.{FileWriter, File}
import scala.io.Source
import java.text.SimpleDateFormat
import com.google.gson._
import java.util.Date
import java.lang.reflect.Type

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
object Utils {
  val configPath = "config"
  val peerIndexPath = configPath + File.separator + "PeerIndexCache.json"
  val localIp = InetAddress.getLocalHost
  val dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")

  def portHash(ipAddress: InetAddress): Int = 9001

  def getCachedPeerIndex: Set[Peer] = {
    val file = Source.fromFile(peerIndexPath)
    Peer.gson.fromJson(file.mkString, classOf[Array[Peer]]).toSet[Peer]
  }

  def cachePeerIndex(peerSet: Set[Peer]) {
    val file = new FileWriter(peerIndexPath)
    file.write(Peer.gson.toJson(peerSet))
  }
}

class DateSerializer extends JsonSerializer[Date] {
  override def serialize(p1: Date, p2: Type, p3: JsonSerializationContext): JsonElement = new JsonPrimitive(Utils.dateParser.format(p1))
}

class DateDeserializer extends JsonDeserializer[Date] {
  override def deserialize(p1: JsonElement, p2: Type, p3: JsonDeserializationContext): Date = Utils.dateParser.parse(p1.getAsJsonPrimitive.getAsString)
}
