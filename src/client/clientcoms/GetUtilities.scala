package clientcoms

import java.net.{HttpURLConnection, URL, InetAddress}
import peer.Peer
import scala.io.BufferedSource
import com.twitter.json.Json

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/17/13
 * Time: 1:02 AM
 * To change this template use File | Settings | File Templates.
 */
package object getutilities {
  private def get(address: InetAddress, extension: String): BufferedSource = {
    val url = new URL(address.getHostAddress + extension)
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    new BufferedSource(connection.getInputStream)
  }

  def ping(address: InetAddress): Boolean = {
    val reader = get(address, "/ping")
    val response = reader.toString
    response == "pong"
  }

  def getPeerList(address: InetAddress): Set[Peer] = {
    val reader = get(address, "/peerlist")
    (for {peer <- Json.parse(reader.toString).asInstanceOf[List[Map[String, String]]]} yield Peer(peer)).toSet
  }
}