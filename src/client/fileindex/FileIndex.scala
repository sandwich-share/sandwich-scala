package fileindex

import java.sql.Time
import java.util.zip.CRC32
import scala.collection.immutable
import com.twitter.json.{Json, JsonSerializable}
import java.util.Date
import java.text.SimpleDateFormat
import utils.Utils

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:50 AM
 * To change this template use File | Settings | File Templates.
 */
case class FileItem(var fileName: String, var size: Long, var checksum: Int) extends JsonSerializable {
  def toJson(): String = Json.build(Map("FileName" -> fileName, "Size" -> size, "Checksum" -> checksum)).toString
}

object FileItem {
  def apply(map: Map[String, String]) = FileItem(map("FileName"), map("Size").toLong, map("Checksum").toInt)
}

class FileIndex(val fileList: Set[FileItem]) extends  JsonSerializable {
  private val crc = new CRC32
  for(FileItem(name, size, checksum) <- fileList) {
    crc.update((for {letter <- name} yield letter.toByte).toArray)
    for(i <- List.range(0, 7)) {
      crc.update((size >> i * 8).toByte)
    }
    for(i <- List.range(0, 3)) {
      crc.update(checksum >> i * 8)
    }
  }
  private var _fileHash = crc.getValue.toInt
  private var _timeStamp = new Date

  def fileHash = _fileHash
  def timeStamp = _timeStamp

  override def toJson(): String = Json.build(Map("IndexHash" -> fileHash.toString,
    "TimeStamp" -> timeStamp.toString, "List" -> Json.build(fileList.toList).toString)).toString

  def this(map: Map[String, Any]) {
    this(map("List").asInstanceOf[List[Map[String, String]]].map(map => FileItem(map)).toSet)
    _fileHash = map("IndexHash").asInstanceOf[String].toInt
    _timeStamp = Utils.dateParser.parse(map("TimeStamp").asInstanceOf[String])
  }
}