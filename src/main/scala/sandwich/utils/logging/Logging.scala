package sandwich.utils.logging

import org.slf4j.{Logger, LoggerFactory}

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 10/20/13
 * Time: 6:24 PM
 */
object Logger {
  def apply(string: String): Logger = LoggerFactory.getLogger(string)
  def apply(clazz: Class): Logger = LoggerFactory.getLogger(clazz)
}

trait Logging {
  lazy val log = LoggerFactory.getLogger(this.getClass)
}
