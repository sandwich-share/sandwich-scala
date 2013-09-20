package fileindex

import java.sql.Time
import java.util.zip.CRC32
import scala.collection.immutable
import com.twitter.json.{Json, JsonSerializable}
import java.util.Date
import java.text.SimpleDateFormat
import utils.{DateDeserializer, DateSerializer, Utils}
import com.google.gson._
import java.lang.reflect.Type

/**
 * Created with IntelliJ IDEA.
 * User: brendan
 * Date: 9/16/13
 * Time: 4:50 AM
 * To change this template use File | Settings | File Templates.
 */
case class FileItem(var FileName: String, var Size: Long, var CheckSum: Int)

object FileItem {
  def gsonBuilder: GsonBuilder = new GsonBuilder
  def gson: Gson = gsonBuilder.create()
}

class FileIndex(val fileList: Set[FileItem]) {
  lazy val fileHash: Int = {
    val crc = new CRC32
    for(FileItem(name, size, checksum) <- fileList) {
      crc.update((for {letter <- name} yield letter.toByte).toArray)
      for(i <- List.range(0, 7)) {
        crc.update((size >> i * 8).toByte)
      }
      for(i <- List.range(0, 3)) {
        crc.update(checksum >> i * 8)
      }
    }
    crc.getValue.toInt
  }

  val timeStamp = new Date
}

object FileIndex {
  def gson: Gson = {
    val gson = FileItem.gsonBuilder
    gson.registerTypeAdapter(classOf[Date], new DateSerializer)
    gson.registerTypeAdapter(classOf[Date], new DateDeserializer)
    gson.registerTypeAdapter(classOf[Set[FileItem]], new FileItemSetSerializer)
    gson.registerTypeAdapter(classOf[Set[FileItem]], new FileItemSetDeserializer)
    gson.create()
  }
}

class FileItemSetSerializer extends JsonSerializer[Set[FileItem]]{
  def serialize(p1: Set[FileItem], p2: Type, p3: JsonSerializationContext): JsonElement = FileItem.gson.toJsonTree(p1.toSet[FileItem], classOf[Array[FileItem]])
}

class FileItemSetDeserializer extends JsonDeserializer[Set[FileItem]]{
  def deserialize(p1: JsonElement, p2: Type, p3: JsonDeserializationContext): Set[FileItem] = FileItem.gson.fromJson(p1, classOf[Array[FileItem]]).toSet[FileItem]
}