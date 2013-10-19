package sandwich

import akka.util.Timeout
import java.nio.file.{Paths, Path}
import java.io.Closeable

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/26/13
 * Time: 7:46 PM
 */
package object utils {
  implicit val timeout = Timeout(5000)
  implicit def toPath(stringPath: String): Path = Paths.get(stringPath)
  class SandwichInitializationException(val message: String) extends Exception

  def using[T, C <: Closeable](any: C)(action: C => T): Option[T] = try {
    Some(action(any))
  } catch {
    case _: Throwable => None
  } finally {
    any.close()
  }

  def using[T, C1 <: Closeable, C2 <: Closeable](any1: C1, any2: C2)(action: (C1, C2) => T): Option[T] = try {
    Some(action(any1, any2))
  } catch {
    case _: Throwable => None
  } finally {
    any1.close()
    any2.close()
  }

  def using[T, C <: Closeable](any: C*)(action: Seq[C] => T): Option[T] = try {
    Some(action(any))
  } catch {
    case _: Throwable => None
  } finally {
    any.foreach(_.close())
  }

  def onSuccess[T](action: () => T): Option[T] = try {
    Some(action())
  } catch {
    case _: Throwable => None
  }

  def notNull[T](action: () => T): Option[T] = {
    val result = action()
    if (result != null) {
      Some(result)
    } else {
      None
    }
  }

  def notNull[T](value: T): Option[T] = if (value != null) Some(value) else None
}