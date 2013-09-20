package clientcoms

import java.net.{HttpURLConnection, URL, InetAddress}
import peer.Peer
import scala.io.BufferedSource
import com.google.gson.Gson
import fileindex.FileIndex

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

  def ping(address: InetAddress): Boolean = try {
    val reader = get(address, "/ping")
    val response = reader.mkString
    response == "pong"
  } catch {
    case _: Throwable => false
  }

  def getPeerList(address: InetAddress): Option[Set[Peer]] = try {
    val reader = get(address, "/peerlist")
    Option(Peer.gson.fromJson(reader.mkString, classOf[Array[Peer]]).toSet[Peer])
  } catch {
    case _: Throwable => Option.empty[Set[Peer]]
  }

  def getFileIndex(address: InetAddress): Option[FileIndex] = try {
    val reader = get(address, "/fileindex")
    Option(FileIndex.gson.fromJson(reader.mkString, classOf[FileIndex]))
  } catch {
    case _: Throwable => Option.empty
  }
}