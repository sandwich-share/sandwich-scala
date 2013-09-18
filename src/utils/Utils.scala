package utils

import java.net.InetAddress
import peer.Peer
import java.io.File
import scala.io.Source
import com.twitter.json.Json
import java.text.SimpleDateFormat

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
object Utils {
  val configPath = "config"
  val localIp = InetAddress.getLocalHost
  val dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ")

  def portHash(ipAddress: InetAddress): Int = 9001

  def getCachedPeerIndex: Set[Peer] = {
    val peerIndexPath = configPath + File.pathSeparator + "PeerIndexCache.json"
    val file = Source.fromFile(peerIndexPath)
    (for {map <- Json.parse(file.mkString).asInstanceOf[List[Map[String, String]]]} yield Peer(map)).toSet
  }
}
