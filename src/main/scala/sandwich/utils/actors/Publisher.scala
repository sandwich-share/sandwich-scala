package sandwich.utils.actors

import akka.actor.{Terminated, ActorRef, Actor}
import scala.collection.mutable
import sandwich.utils.logging.Logging

/**
 * An actor that produces a special value type T and broadcasts it to subscribers.
 * @tparam T the type to publish.
 */
abstract class Publisher[T] extends ActorBase {
  private val subscribers = mutable.Set[ActorRef]()

  /**
   * Defines the behavior of how and when subscribers are notified of a broadcast value type T.
   * By default it listens for the broadcast value type T and broadcasts it when it hears it.
   * @param subscribers to be notified on a broadcast event
   * @return a partial function to be grafted into receive that broadcasts the special broadcast value.
   */
  protected def broadcast(subscribers: mutable.Set[ActorRef]): PartialFunction[Any, Unit] = {
    case broadcastValue: T => subscribers.foreach(_ ! broadcastValue)
  }

  /**
   * This defines the behavior of a publisher: it keeps track of subscribers and broadcasts a special value to them
   * defined by broadcastResult in the core.
   * @return the partial function representing the behavior of a Publisher
   */
  override def receive = PartialFunction[Any, Unit] {
    case broadcastValue: T => subscribers.foreach(_ ! broadcastValue)

    case ref: ActorRef => {
      context.watch(ref)
      subscribers += ref
      log.info("Now listening to: " + ref)
    }

    case Terminated(ref) => {
      context.unwatch(ref)
      subscribers -= ref
      log.info("No longer listening to: " + ref)
    }
  } orElse broadcast(subscribers)
}