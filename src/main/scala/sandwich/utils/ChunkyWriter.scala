package sandwich.utils

import java.io._

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/4/13
 * Time: 2:02 PM
 */
// TODO: We might want to choose a better default chunkSize.
class ChunkyWriter(val writer: OutputStream, val chunkSize: Int = 4 * 1024 * 1024) {

  def write(reader: InputStream, totalAmountToRead: Long) {
    var amountLeftToRead = totalAmountToRead
    val chunk = new Array[Byte](chunkSize)
    do {
      var amountToWrite = 0
      try {
        amountToWrite = reader.read(chunk)
      } catch {
        case eof: EOFException => println(eof)
        case error: Throwable => println(error)
      }
      writer.write(chunk, 0, amountToWrite)
      amountLeftToRead -= amountToWrite
      println("Amount being read:" + amountToWrite)
      println("Amount left to read: " + amountLeftToRead)
    } while (amountLeftToRead != 0)
  }
}
