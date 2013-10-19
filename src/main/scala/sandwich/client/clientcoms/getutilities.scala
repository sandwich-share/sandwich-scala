package sandwich.client.clientcoms

import java.net.{URI, HttpURLConnection, URL, InetAddress}
import sandwich.client.peer.Peer
import scala.io.BufferedSource
import sandwich.client.fileindex.FileIndex
import java.nio.file.{Paths, Files, Path}
import java.io._
import sandwich.utils.{Settings, Utils, ChunkyWriter}
import scala.concurrent.{Future, future}
import scala.concurrent.ExecutionContext.Implicits.global
import sandwich.utils._
import scala.Some

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/17/13
 * Time: 1:02 AM
 * To change this template use File | Settings | File Templates.
 */
package object getutilities {
  private def buildURL(address: InetAddress, extension: String) = new URL("http://" + address.getHostAddress + ":" + Utils.portHash(address) + extension)

  private def get(address: InetAddress, extension: String): Option[InputStream] = {
    val url = buildURL(address, extension)
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    val reader = try {
      connection.getInputStream
    } catch {
      case error: Throwable => {
        println(error)
        return None
      }
    }
    Some(reader)
  }

  def ping(address: InetAddress): Boolean = get(address, "/ping").flatMap(using(_) {
    source => new BufferedSource(source).mkString
  }) match {
    case Some(response) => response == "pong\n"
    case None => false
  }

  def getPeerList(address: InetAddress): Option[Set[Peer]] = get(address, "/peerlist").flatMap(using(_) {
    source => Peer.gson.fromJson(new BufferedSource(source).mkString, classOf[Array[Peer]]).toSet
  })

  def getFileIndex(peer: Peer): Future[Option[FileIndex]] = future {
    get(peer.IP, "/fileindex").flatMap(using(_) {
      source => FileIndex.gson.fromJson(new BufferedSource(source).mkString, classOf[FileIndex])
    })
  }

  def getFileIndices(peerSet: Set[Peer]): Set[(Peer, Future[Option[FileIndex]])] = peerSet.map(peer => (peer, getFileIndex(peer)))

  def getFile(address: InetAddress, path: Path) {
    val localPath = Paths.get(Settings.getSettings.sandwichPath + File.separator + path)
    onSuccess(new URI("http", null, address.getHostAddress, Utils.portHash(address), "/files/" + path, null, null).toURL).foreach { url =>
      val connection = url.openConnection.asInstanceOf[HttpURLConnection]
      val file = localPath.toFile
      onSuccess(file.getParentFile).flatMap(notNull(_)).flatMap(file => onSuccess(() => file.mkdirs()))
      onSuccess(() => file.createNewFile())
      future {
        using(connection.getInputStream, new FileOutputStream(file)) { (connectionReader: InputStream, fileWriter: OutputStream) =>
          new ChunkyWriter(fileWriter).write(connectionReader, connection.getContentLengthLong)
        }
      }
    }
  }
}