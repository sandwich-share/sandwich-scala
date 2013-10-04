package sandwich

import sandwich.controller.Controller
import akka.actor.ActorSystem

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/26/13
 * Time: 7:32 PM
 */
object Main {
  val system = ActorSystem("SandwichSystem")
  val controller = system.actorOf(Controller.props)
  def main(args: Array[String]) {
    println("Hello world!")
  }
}
