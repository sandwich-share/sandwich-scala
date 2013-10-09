package sandwich.client.clientcoms

import java.net.{URI, HttpURLConnection, URL, InetAddress}
import sandwich.client.peer.Peer
import scala.io.BufferedSource
import sandwich.client.fileindex.FileIndex
import java.nio.file.{Paths, Files, Path}
import java.io.{File, FileWriter, InputStreamReader}
import sandwich.utils.{Settings, Utils, ChunkyWriter}
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/17/13
 * Time: 1:02 AM
 * To change this template use File | Settings | File Templates.
 */
package object getutilities {
  private def buildURL(address: InetAddress, extension: String) = new URL("http://" + address.getHostAddress + ":" + Utils.portHash(address) + extension)

  private def get(address: InetAddress, extension: String): BufferedSource = {
    val url = buildURL(address, extension)
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    new BufferedSource(connection.getInputStream)
  }

  def ping(address: InetAddress): Boolean = try {
    val reader = get(address, "/ping")
    val response = reader.mkString
    reader.close()
    response == "pong\n"
  } catch {
    case error: Throwable => {
      println(error)
      false
    }
  }

  def getPeerList(address: InetAddress): Option[Set[Peer]] = try {
    val reader = get(address, "/peerlist")
    val result = Option(Peer.gson.fromJson(reader.mkString, classOf[Array[Peer]]).toSet[Peer])
    reader.close()
    return result
  } catch {
    case error: Throwable => {
      println(error)
      None
    }
  }

  def getFileIndex(address: InetAddress): Option[FileIndex] = try {
    val reader = get(address, "/fileindex")
    val result = Option(FileIndex.gson.fromJson(reader.mkString, classOf[FileIndex]))
    reader.close()
    return result
  } catch {
    case _: Throwable => None
  }

  def getFile(address: InetAddress, path: Path) {
    // TODO: We should guarantee that failures clean themselves up.
    try {
      val url = new URI("http", null, address.getHostAddress, Utils.portHash(address), "/files/" + path, null, null).toURL
      val localPath = Paths.get(Settings.getSettings.sandwichPath + File.separator + path)
      val connection = new InputStreamReader(url.openConnection.asInstanceOf[HttpURLConnection].getInputStream)
      println("Requesting: " + url)
      val file = localPath.toFile
      val parentDir = file.getParentFile
      if (parentDir != null) {
        parentDir.mkdirs()
      }
      file.createNewFile()
      println("File exists: " + file.exists())
      println("Downloading file: " + localPath)
      val fileWriter = new FileWriter(file)
      val chunkyWriter = new ChunkyWriter(fileWriter)
      future {
        chunkyWriter.write(connection)
        fileWriter.close()
        connection.close()
      }
    } catch {
      case error: Throwable => println(error)
    }
  }
}