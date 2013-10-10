package sandwich.utils

import java.io._

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/4/13
 * Time: 2:02 PM
 */
// TODO: We might want to choose a better default chunkSize.
class ChunkyWriter(private val writer: OutputStream, val chunkSize: Int = 64 * 1024) {

  def write(reader: InputStream, totalAmountToRead: Long) {
    var amountLeftToRead = totalAmountToRead
    val chunk = new Array[Byte](chunkSize)
    do {
      var amountToWrite = reader.read(chunk)
      writer.write(chunk, 0, amountToWrite)
      amountLeftToRead -= amountToWrite
    } while (amountLeftToRead != 0)
  }
}
