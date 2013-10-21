package sandwich.client.clientcoms

import java.net.{URI, HttpURLConnection, URL, InetAddress}
import sandwich.client.peer.Peer
import scala.io.BufferedSource
import sandwich.client.fileindex.FileIndex
import java.nio.file.{Paths, Path}
import java.io._
import scala.concurrent.{Future, future}
import scala.concurrent.ExecutionContext.Implicits.global
import sandwich.utils._
import scala.Some
import java.util.zip.GZIPInputStream

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/17/13
 * Time: 1:02 AM
 * To change this template use File | Settings | File Templates.
 */
package object getutilities {
  private def buildURL(address: InetAddress, extension: String) = new URL("http://" + address.getHostAddress + ":" + Utils.portHash(address) + extension)

  private def get(address: InetAddress, extension: String, requestProperties: Map[String, String] = Map()): Option[InputStream] = {
    val connection = onSuccess(() => buildURL(address, extension).openConnection.asInstanceOf[HttpURLConnection])
    requestProperties.foreach { case (key, value) => connection.flatMap(connection => onSuccess(() => connection.setRequestProperty(key, value))) }
    connection.flatMap(connection => onSuccess(connection.getInputStream))
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
    val connection = onSuccess(() => buildURL(peer.IP, "/fileindex").openConnection().asInstanceOf[HttpURLConnection])
    connection.flatMap(connection => onSuccess(() => connection.setRequestProperty("Accept-Encoding", "gzip")))
    connection.flatMap(connection => onSuccess {() =>
      using(connection.getInputStream) { connectionStream =>
        val inputStream = if (connection.getContentEncoding == "gzip") {
          new GZIPInputStream(connectionStream)
        } else connectionStream
        FileIndex.gson.fromJson(new BufferedSource(inputStream).mkString, classOf[FileIndex])
      }
    }).flatten
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