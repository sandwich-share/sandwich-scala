package sandwich.utils

import java.io.{Reader, Writer}

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/4/13
 * Time: 2:02 PM
 */
// TODO: We might want to choose a better default chunkSize.
class ChunkyWriter(val writer: Writer, val chunkSize: Int = 5120) {

  def write(reader: Reader) {
    var done = false
    val chunk = new Array[Char](chunkSize)
    while (!done) {
      val amountToWrite = reader.read(chunk)
      if (amountToWrite < chunkSize) {
        writer.write(chunk, 0, amountToWrite)
        done = true
      } else {
        writer.write(chunk)
      }
    }
  }
}
