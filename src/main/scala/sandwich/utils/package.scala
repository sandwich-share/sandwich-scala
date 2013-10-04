package sandwich

import akka.util.Timeout

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/26/13
 * Time: 7:46 PM
 */
package object utils {
  implicit val timeout = Timeout(5000)
  case class SandwichInitializationException(val message: String) extends Exception
}
