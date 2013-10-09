package sandwich.utils

import java.net._
import sandwich.client.peer.Peer
import java.io.{FileWriter, File}
import scala.io.Source
import java.text.SimpleDateFormat
import com.google.gson._
import java.util.Date
import java.lang.reflect.Type
import java.security.MessageDigest
import scala.concurrent.{Future, future}
import scala.concurrent.ExecutionContext.Implicits.global

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
  val localIp = getLocalIp
  val dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")

  def unsign(signed: Byte): Int = if(signed >= 0) {
    signed
  } else {
    signed + 256
  }

  def portHash(ipAddress: InetAddress): Int = {
    var hash = Array[Byte]()
    var address = Array[Byte]()
    var port: Int = 0
    val messageDigest = MessageDigest.getInstance("MD5")
    ipAddress match {
      case ip: Inet4Address => {
        address = new Array[Byte](ip.getAddress.size + 12)
        System.arraycopy(ip.getAddress, 0, address, 12, ip.getAddress.size)
        address(10) = 0xff.asInstanceOf[Byte]
        address(11) = 0xff.asInstanceOf[Byte]
      }
      case ip: Inet6Address => {
        address = ip.getAddress
      }
    }
    hash = messageDigest.digest(address)
    port = (unsign(hash(0)) + unsign(hash(3))) << 8
    port += unsign(hash(1)) + unsign(hash(2))
    port &= 0xFFFF
    if(port < 1024) {
      port + 1024
    } else {
      port
    }
  }

  def getLocalIp: InetAddress = try {
    val response = Source.fromInputStream(new URL("http://www.curlmyip.com").openStream()).mkString
    InetAddress.getByName(response)
  } catch {
    case _: Throwable => InetAddress.getLocalHost
  }

  def getCachedPeerIndex(): Future[Set[Peer]] = future {
    val file = Source.fromFile(peerIndexPath)
    val peerSet = Peer.gson.fromJson(file.mkString, classOf[Array[Peer]])
    peerSet.toSet[Peer]
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
