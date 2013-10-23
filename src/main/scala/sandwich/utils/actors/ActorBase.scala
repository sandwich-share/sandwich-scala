package sandwich.utils.actors

import akka.actor.Actor
import sandwich.utils.logging.Logging

/**
 * We need to have a non-abstract base class to inherit from to do the type magic in Subscribing
 */
class ActorBase extends Actor with Logging {
  override def receive: PartialFunction[Any, Unit] = PartialFunction.empty[Any, Unit]
  override def preStart() = initialize()
  def initialize() {}
}
