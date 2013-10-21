package sandwich.client.fileindex

import akka.agent.Agent
import sandwich.client.peer.Peer
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.{OutputStreamWriter, ByteArrayOutputStream}
import java.util.zip.GZIPOutputStream
import sandwich.utils.logging.Logging

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/21/13
 * Time: 2:33 AM
 */
class FileIndexContainer {
  private val fileIndexHashAgent = Agent[Long](0)
  private val fileIndexJsonAgent = Agent[String]("")
  private val fileIndexGZIPAgent = Agent[Array[Byte]](Array())

  def update(fileIndex: FileIndex) {
    fileIndexHashAgent.send(fileIndex.IndexHash)
    fileIndexJsonAgent.send(Peer.gson.toJson(fileIndex))
    val buffer = new ByteArrayOutputStream()
    val gzipStream = new OutputStreamWriter(new GZIPOutputStream(buffer))
    gzipStream.write(fileIndexJsonAgent())
    fileIndexGZIPAgent.send(buffer.toByteArray)
  }

  def fileIndexHash = fileIndexHashAgent()

  def fileIndexJson = fileIndexJsonAgent()

  def fileIndexGZIP = fileIndexGZIPAgent()
}